/*
 * Copyright (C) 2014-2020 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.midpoint.service;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.impl.query.builder.QueryBuilder;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

@Service
@Produces({ "application/xml", "application/json", "application/yaml" })
public class ExampleRestService extends AbstractRestService {

    private static final Trace LOGGER = TraceManager.getTrace(ExampleRestService.class);

    @Autowired private ModelService modelService;
    @Autowired private PrismContext prismContext;

    @GET
    @Path("/users/mail/{email}")
    public Response searchUserByEmail(@PathParam("email") String email) {
        LOGGER.info("searchUserByEmail called with email = {}", email);
        Task task = initRequest();
        OperationResult result = createSubresult(task, "searchUserByEmail");

        Response response;
        try {
            List<PrismObject<UserType>> users = findUsers(email, task, result);
            ObjectListType listType = new ObjectListType();
            for (PrismObject<UserType> user : users) {
                listType.getObject().add(user.asObjectable());
            }
            response = Response.ok().entity(listType).build();
        } catch (CommonException | RuntimeException e) {
            response = handleException(null, e);
        }

        result.computeStatus();
        finishRequest(task);
        return response;
    }

    private <T> List<PrismObject<UserType>> findUsers(T email, Task task, OperationResult result)
            throws SchemaException, ObjectNotFoundException, SecurityViolationException,
            CommunicationException, ConfigurationException, ExpressionEvaluationException {

        ObjectQuery query = QueryBuilder.queryFor(UserType.class, prismContext)
                .item(UserType.F_EMAIL_ADDRESS).startsWith(email).matchingCaseIgnore()
                .build();
        return modelService.searchObjects(UserType.class, query, null, task, result);
    }
}
