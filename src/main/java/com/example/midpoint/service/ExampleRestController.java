/*
 * Copyright (C) 2010-2022 Evolveum
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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evolveum.midpoint.model.api.ModelService;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.impl.query.builder.QueryBuilder;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.rest.impl.AbstractRestController;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * This is a non-CXF variant based on Spring MVC.
 * This is derived from midPoint public but non-API class {@link AbstractRestController}.
 * It is generally usable (for a couple of MP versions already), but no support is provided.
 */
@Experimental
@RestController
@RequestMapping("/ws/my-rest")
@SuppressWarnings("unused")
public class ExampleRestController extends AbstractRestController {

    private final ModelService modelService;
    private final PrismContext prismContext;

    public ExampleRestController(ModelService modelService, PrismContext prismContext) {
        this.modelService = modelService;
        this.prismContext = prismContext;
    }

    @GetMapping("/users/mail/{email}")
    public ResponseEntity<?> searchUserByEmail(@PathVariable("email") String email) {
        logger.info("searchUserByEmail called with email = {}", email);
        Task task = initRequest();
        OperationResult result = createSubresult(task, "searchUserByEmail");

        ResponseEntity<?> response;
        try {
            List<PrismObject<UserType>> users = findUsers(email, task, result);
            ObjectListType listType = new ObjectListType();
            for (PrismObject<UserType> user : users) {
                listType.getObject().add(user.asObjectable());
            }
            response = createResponse(HttpStatus.OK, listType, result);
        } catch (CommonException | RuntimeException e) {
            response = handleException(null, e);
        }
        result.computeStatus();
        finishRequest(task, result);
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
