/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.app.api.v1.resources;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.kapua.app.api.v1.resources.model.CountResult;
import org.eclipse.kapua.app.api.v1.resources.model.DateParam;
import org.eclipse.kapua.app.api.v1.resources.model.ScopeId;
import org.eclipse.kapua.app.api.v1.resources.model.StorableEntityId;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.datastore.DatastoreObjectFactory;
import org.eclipse.kapua.service.datastore.MessageStoreService;
import org.eclipse.kapua.service.datastore.internal.elasticsearch.ChannelInfoField;
import org.eclipse.kapua.service.datastore.internal.elasticsearch.MessageField;
import org.eclipse.kapua.service.datastore.internal.model.query.AndPredicateImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.ChannelMatchPredicateImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.RangePredicateImpl;
import org.eclipse.kapua.service.datastore.model.DatastoreMessage;
import org.eclipse.kapua.service.datastore.model.MessageListResult;
import org.eclipse.kapua.service.datastore.model.query.AndPredicate;
import org.eclipse.kapua.service.datastore.model.query.ChannelMatchPredicate;
import org.eclipse.kapua.service.datastore.model.query.MessageQuery;
import org.eclipse.kapua.service.datastore.model.query.RangePredicate;
import org.eclipse.kapua.service.datastore.model.query.StorableFetchStyle;
import org.eclipse.kapua.service.datastore.model.query.TermPredicate;

import com.google.common.base.Strings;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api("Data Messages")
@Path("{scopeId}/data/messages")
public class DataMessages extends AbstractKapuaResource {

    private final KapuaLocator locator = KapuaLocator.getInstance();
    private final MessageStoreService messageRegistryService = locator.getService(MessageStoreService.class);
    private final DatastoreObjectFactory datastoreObjectFactory = locator.getFactory(DatastoreObjectFactory.class);

    /**
     * Gets the {@link DatastoreMessage} list in the scope.
     *
     * @param scopeId
     *            The {@link ScopeId} in which to search results.
     * @param clientId
     *            The client id to filter results.
     * @param channel
     *            The channel id to filter results. It allows '#' wildcard in last channel level.
     * @param startDate
     *            The start date to filter the results. Must come before endDate parameter.
     * @param endDate
     *            The end date to filter the results. Must come after startDate parameter
     * @param offset
     *            The result set offset.
     * @param limit
     *            The result set limit.
     * @return The {@link MessageListResult} of all the datastoreMessages associated to the current selected scope.
     * @since 1.0.0
     */
    @ApiOperation(value = "Gets the DatastoreMessage list in the scope", //
            notes = "Returns the list of all the datastoreMessages associated to the current selected scope.", //
            response = DatastoreMessage.class, //
            responseContainer = "DatastoreMessageListResult")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public MessageListResult simpleQuery(  //
            @ApiParam(value = "The ScopeId in which to search results", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId,//
            @ApiParam(value = "The client id to filter results") @QueryParam("clientId") String clientId, //
            @ApiParam(value = "The channel to filter results. It allows '#' wildcard in last channel level") @QueryParam("channel") String channel,
            @ApiParam(value = "The start date to filter the results. Must come before endDate parameter") @QueryParam("startDate") DateParam startDateParam,
            @ApiParam(value = "The end date to filter the results. Must come after startDate parameter") @QueryParam("endDate") DateParam endDateParam,
            // @ApiParam(value = "The metric name to filter results") @QueryParam("metricName") String metricName, //
            // @ApiParam(value = "The metric type to filter results") @QueryParam("metricType") MetricType metricType, //
            // @ApiParam(value = "The min metric value to filter results") @QueryParam("metricValueMin") String metricMinValue, //
            // @ApiParam(value = "The max metric value to filter results") @QueryParam("metricValueMax") String metricMaxValue, //
            @ApiParam(value = "The result set offset", defaultValue = "0") @QueryParam("offset") @DefaultValue("0") int offset,//
            @ApiParam(value = "The result set limit", defaultValue = "50") @QueryParam("limit") @DefaultValue("50") int limit) //
    {
        MessageListResult datastoreMessageListResult = datastoreObjectFactory.newDatastoreMessageListResult();
        try {
            AndPredicate andPredicate = new AndPredicateImpl();
            if (!Strings.isNullOrEmpty(clientId)) {
                TermPredicate clientIdPredicate = datastoreObjectFactory.newTermPredicate(MessageField.CLIENT_ID, clientId);
                andPredicate.getPredicates().add(clientIdPredicate);
            }

            if (!Strings.isNullOrEmpty(channel)) {
                ChannelMatchPredicate channelPredicate = new ChannelMatchPredicateImpl(channel);
                andPredicate.getPredicates().add(channelPredicate);
            }
            
            Date startDate = startDateParam != null ? startDateParam.getDate() : null;
            Date endDate = endDateParam != null ? endDateParam.getDate() : null;
            if ( startDate != null || endDate != null) {
                RangePredicate timestampPredicate = new RangePredicateImpl(ChannelInfoField.TIMESTAMP, startDate, endDate);
                andPredicate.getPredicates().add(timestampPredicate);
            }

            // manageMetricValueFiltering(andPredicate, metricName, metricType, metricMinValue, metricMaxValue);

            MessageQuery query = datastoreObjectFactory.newDatastoreMessageQuery(scopeId);
            query.setPredicate(andPredicate);
            query.setOffset(offset);
            query.setLimit(limit);

            datastoreMessageListResult = query(scopeId, query);
        } catch (Throwable t) {
            handleException(t);
        }
        return datastoreMessageListResult;
    }

    /**
     * Queries the results with the given {@link DatastorMessageQuery} parameter.
     * 
     * @param scopeId
     *            The {@link ScopeId} in which to search results.
     * @param query
     *            The {@link DatastorMessageQuery} to used to filter results.
     * @return The {@link MessageListResult} of all the result matching the given {@link DatastorMessageQuery} parameter.
     * @since 1.0.0
     */
    @POST
    @Path("_query")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Queries the DatastoreMessages", //
            notes = "Queries the DatastoreMessages with the given DatastoreMessageQuery parameter returning all matching DatastoreMessages",  //
            response = DatastoreMessage.class, //
            responseContainer = "DatastoreMessageListResult")
    public MessageListResult query( //
            @ApiParam(value = "The ScopeId in which to search results", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId, //
            @ApiParam(value = "The DatastoreMessageQuery to use to filter results", required = true) MessageQuery query) {
        MessageListResult datastoreMessageListResult = null;
        try {
            query.setScopeId(scopeId);
            datastoreMessageListResult = messageRegistryService.query(query);
        } catch (Throwable t) {
            handleException(t);
        }
        return returnNotNullEntity(datastoreMessageListResult);
    }

    /**
     * Counts the results with the given {@link DatastorMessageQuery} parameter.
     * 
     * @param scopeId
     *            The {@link ScopeId} in which to search results.
     * @param query
     *            The {@link DatastorMessageQuery} to used to filter results.
     * @return The count of all the result matching the given {@link DatastorMessageQuery} parameter.
     * @since 1.0.0
     */
    @POST
    @Path("_count")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Counts the DatastoreMessages", //
            notes = "Counts the DatastoreMessages with the given DatastoreMessageQuery parameter returning the number of matching DatastoreMessages", //
            response = CountResult.class)
    public CountResult count( //
            @ApiParam(value = "The ScopeId in which to search results", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId, //
            @ApiParam(value = "The DatastoreMessageQuery to use to filter count results", required = true) MessageQuery query) {
        CountResult countResult = null;
        try {
            query.setScopeId(scopeId);
            countResult = new CountResult(messageRegistryService.count(query));
        } catch (Throwable t) {
            handleException(t);
        }
        return returnNotNullEntity(countResult);
    }

    /**
     * Returns the DatastoreMessage specified by the "datastoreMessageId" path parameter.
     *
     * @param datastoreMessageId
     *            The id of the requested DatastoreMessage.
     * @return The requested DatastoreMessage object.
     */
    @GET
    @Path("{datastoreMessageId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Gets an DatastoreMessage", //
            notes = "Gets the DatastoreMessage specified by the datastoreMessageId path parameter", //
            response = DatastoreMessage.class)
    public DatastoreMessage find( //
            @ApiParam(value = "The ScopeId of the requested DatastoreMessage.", required = true, defaultValue = DEFAULT_SCOPE_ID) @PathParam("scopeId") ScopeId scopeId,
            @ApiParam(value = "The id of the requested DatastoreMessage", required = true) @PathParam("datastoreMessageId") StorableEntityId datastoreMessageId) {
        DatastoreMessage datastoreMessage = null;
        try {
            datastoreMessage = messageRegistryService.find(scopeId, datastoreMessageId, StorableFetchStyle.SOURCE_FULL);
        } catch (Throwable t) {
            handleException(t);
        }
        return returnNotNullEntity(datastoreMessage);
    }

    // /**
    // * Manages the optional metric value filtering for {@link #simpleQuery(ScopeId, String, String, String, String, String, String, int, int)} invocation.
    // *
    // * @param andPredicate
    // * The {@link AndPredicate} container
    // * @param metricName
    // * The metric name to filter. Mandatory.
    // * @param metricType
    // * The metric type to filter. Mandatory.
    // * @param metricMinValue
    // * The metric minimum value to filter. It must be coherent with the type parameter.
    // * @param metricMaxValue
    // * The metric maximum value to filter. It must be coherent with the type parameter.
    // * @throws KapuaIllegalNullArgumentException
    // * If metricName or metricType parameter are null.
    // * @throws KapuaIllegalArgumentException
    // * If metricType is not managed.
    // */
    // private <T extends Comparable<T>> void manageMetricValueFiltering(AndPredicate andPredicate, String metricName, MetricType metricType, String metricMinValue, String metricMaxValue)
    // throws KapuaIllegalNullArgumentException, KapuaIllegalArgumentException {
    //
    // if (metricName != null && metricType != null && metricMinValue != null && metricMaxValue != null) {
    //
    // if (Strings.isNullOrEmpty(metricName)) {
    // throw new KapuaIllegalNullArgumentException("metricName");
    // }
    //
    // T convertedMinValue;
    // try {
    // convertedMinValue = parseMetricValue(metricType, metricMinValue);
    // } catch (NumberFormatException nfe) {
    // throw new KapuaIllegalArgumentException("metricMinValue", metricMinValue);
    // }
    //
    // T convertedMaxValue;
    // try {
    // convertedMaxValue = parseMetricValue(metricType, metricMaxValue);
    // } catch (NumberFormatException nfe) {
    // throw new KapuaIllegalArgumentException("metricMinValue", metricMinValue);
    // }
    //
    // andPredicate.getPredicates().add(new RangePredicateImpl<>(MessageField.METRICS, convertedMinValue, convertedMaxValue));
    // }
    // }
    //
    // @SuppressWarnings("unchecked")
    // private <T> T parseMetricValue(MetricType metricType, String metricValue) throws KapuaIllegalArgumentException {
    // Class<T> metricTypeClazz = metricType.getClazz();
    //
    // T convertedValue;
    // if (metricValue == null) {
    // convertedValue = null;
    // } else if (metricTypeClazz == String.class) {
    // convertedValue = (T) metricValue;
    // } else if (metricTypeClazz == Integer.class) {
    // convertedValue = (T) new Integer(metricValue);
    // } else if (metricTypeClazz == Long.class) {
    // convertedValue = (T) new Long(metricValue);
    // } else if (metricTypeClazz == Float.class) {
    // convertedValue = (T) new Float(metricValue);
    // } else if (metricTypeClazz == Double.class) {
    // convertedValue = (T) new Double(metricValue);
    // } else if (metricTypeClazz == Boolean.class) {
    // convertedValue = (T) new Boolean(metricValue);
    // } else {
    // throw new KapuaIllegalArgumentException("metricType", metricTypeClazz.getSimpleName());
    // }
    // return convertedValue;
    // }
}
