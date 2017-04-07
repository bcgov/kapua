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
 *
 *******************************************************************************/
package org.eclipse.kapua.app.console.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.kapua.service.datastore.model.DatastoreMessage;

import com.opencsv.CSVWriter;

public class DataExporterCsv extends DataExporter {

    private CSVWriter writer;
    private String topicOrAsset;
    private String[] headers;

    protected DataExporterCsv(HttpServletResponse response, String topicOrAsset) {
        super(response);
        this.topicOrAsset = topicOrAsset;
    }

    @Override
    public void init(String[] headers) throws ServletException, IOException {
        this.headers = headers;

        OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), Charset.forName("UTF-8"));
        writer = new CSVWriter(osw);
        List<String> columns = new ArrayList<String>();
        for (String column : mandatoryColumns) {
            columns.add(column);
        }
        for (String header : headers) {
            columns.add(header);
        }
        writer.writeNext(columns.toArray(new String[] {}));
    }

    @Override
    public void append(List<DatastoreMessage> messages) throws ServletException, IOException {
        for (DatastoreMessage message : messages) {
            String topic = BLANK;
            List<String> columns = new ArrayList<>();
            columns.add(valueOf(message.getTimestamp()));
            columns.add(valueOf(message.getClientId()));
            if (message.getChannel() != null) {
                List<String> semanticParts = message.getChannel().getSemanticParts();
                StringBuilder semanticTopic = new StringBuilder();
                for (int i = 0; i < semanticParts.size() - 1; i++) {
                    semanticTopic.append(semanticParts.get(i));
                    semanticTopic.append("/");
                }
                semanticTopic.append(semanticParts.get(semanticParts.size() - 1));
                topic = semanticTopic.toString();
            }
            columns.add(topic);
            if (message.getPayload() != null && message.getPayload().getProperties() != null) {
                for (String header : headers) {
                    columns.add(valueOf(message.getPayload().getProperties().get(header)));
                }
            }
            writer.writeNext(columns.toArray(new String[] {}));
        }
    }

    @Override
    public void close() throws ServletException, IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(topicOrAsset, "UTF-8") + "_data.csv");
        response.setHeader("Cache-Control", "no-transform, max-age=0");

        writer.flush();

        writer.close();
    }

}
