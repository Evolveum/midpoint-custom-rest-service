/*
 * Copyright (C) 2010-2021 Evolveum
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

import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;

import com.evolveum.midpoint.model.impl.security.SecurityHelper;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.ConnectionEnvironment;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

/**
 * Common functionality that can be reused for concrete subclasses.
 * It also provides (non-static) logger named after concrete class.
 * <p>
 * This replaces RestServiceUtil from midPoint 4.1 that is gone in 4.2 and later.
 */
public abstract class AbstractRestService {

    protected final Trace logger = TraceManager.getTrace(getClass());

    private final String opNamePrefix = getClass().getName() + ".";

    @Autowired protected TaskManager taskManager;
    @Autowired protected SecurityHelper securityHelper;

    protected Task initRequest() {
        // No need to audit login. it was already audited during authentication
        Task task = taskManager.createTaskInstance(opNamePrefix + "restService");
        task.setChannel(SchemaConstants.CHANNEL_REST_URI);
        return task;
    }

    protected OperationResult createSubresult(Task task, String operation) {
        return task.getResult().createSubresult(opNamePrefix + operation);
    }

    protected Response handleException(OperationResult result, Throwable t) {
        LoggingUtils.logUnexpectedException(logger,
                "Got exception while servicing REST request: {}",
                t, result != null ? result.getOperation() : "(null)");
        return handleExceptionNoLog(result, t);
    }

    protected Response handleExceptionNoLog(OperationResult result, Throwable t) {
        return createErrorResponseBuilder(result, t).build();
    }

    protected Response.ResponseBuilder createErrorResponseBuilder(
            OperationResult result, Throwable t) {
        if (t instanceof ObjectNotFoundException) {
            return createErrorResponseBuilder(Response.Status.NOT_FOUND, result);
        }

        if (t instanceof CommunicationException || t instanceof TunnelException) {
            return createErrorResponseBuilder(Response.Status.GATEWAY_TIMEOUT, result);
        }

        if (t instanceof SecurityViolationException) {
            return createErrorResponseBuilder(Response.Status.FORBIDDEN, result);
        }

        if (t instanceof ConfigurationException) {
            return createErrorResponseBuilder(Response.Status.BAD_GATEWAY, result);
        }

        if (t instanceof SchemaException || t instanceof ExpressionEvaluationException) {
            return createErrorResponseBuilder(Response.Status.BAD_REQUEST, result);
        }

        if (t instanceof PolicyViolationException
                || t instanceof ObjectAlreadyExistsException
                || t instanceof ConcurrencyException) {
            return createErrorResponseBuilder(Response.Status.CONFLICT, result);
        }

        return createErrorResponseBuilder(Response.Status.INTERNAL_SERVER_ERROR, result);
    }

    protected Response.ResponseBuilder createErrorResponseBuilder(Response.Status status, OperationResult result) {
        OperationResultType resultBean;
        if (result != null) {
            result.computeStatusIfUnknown();
            resultBean = result.createOperationResultType();
        } else {
            resultBean = null;
        }
        return createErrorResponseBuilder(status, resultBean);
    }

    protected Response.ResponseBuilder createErrorResponseBuilder(Response.Status status, OperationResultType message) {
        return Response.status(status).entity(message);
    }

    protected void finishRequest(Task task) {
        task.getResult().computeStatus();
        ConnectionEnvironment connEnv = ConnectionEnvironment.create(SchemaConstants.CHANNEL_REST_URI);
        connEnv.setSessionIdOverride(task.getTaskIdentifier());
        securityHelper.auditLogout(connEnv, task, task.getResult());
    }
}
