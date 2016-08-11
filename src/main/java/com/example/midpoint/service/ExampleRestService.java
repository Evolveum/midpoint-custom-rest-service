/**
 * Copyright (c) 2014-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.midpoint.service;

import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.model.impl.security.SecurityHelper;
import com.evolveum.midpoint.model.impl.util.RestServiceUtil;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author semancik
 * @author katkav
 */

@Service
@Produces({"application/xml", "application/json"})
public class ExampleRestService {

	private static final Trace LOGGER = TraceManager.getTrace(ExampleRestService.class);

	private static final String OPERATION_SEARCH_USER_BY_EMAIL = ExampleRestService.class.getName() + ".searchUserByEmail";

	@Autowired
	private ModelService modelService;

	@Autowired
	private SecurityHelper securityHelper;

	@Autowired
	private PrismContext prismContext;

	@GET
	@Path("/users/mail/{email}")
	public Response searchUserByEmail(@PathParam("email") String email, @Context MessageContext mc) {
		LOGGER.info("searchUserByEmail called with email = {}", email);
		Task task = RestServiceUtil.initRequest(mc);
		OperationResult result = task.getResult().createSubresult(OPERATION_SEARCH_USER_BY_EMAIL);

		Response response;
		try {
			List<PrismObject<UserType>> users = findUsers(email, task, result);
			ObjectListType listType = new ObjectListType();
			for (PrismObject<UserType> user : users) {
				listType.getObject().add(user.asObjectable());
			}
			response = Response.ok().entity(listType).build();
		} catch (CommonException|RuntimeException e) {
			response = RestServiceUtil.handleException(e);
		}

		result.computeStatus();
		RestServiceUtil.finishRequest(task, securityHelper);
		return response;
	}

	private <T> List<PrismObject<UserType>> findUsers(T email, Task task, OperationResult result)
			throws SchemaException, ObjectNotFoundException,
			SecurityViolationException, CommunicationException, ConfigurationException {

		ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
				.item(UserType.F_EMAIL_ADDRESS).startsWith(email).matchingCaseIgnore()
				.build();
		return modelService.searchObjects(UserType.class, query, null, task, result);
	}
}

