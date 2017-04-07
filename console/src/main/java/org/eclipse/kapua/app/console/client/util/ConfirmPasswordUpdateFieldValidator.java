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
package org.eclipse.kapua.app.console.client.util;

import org.eclipse.kapua.app.console.client.messages.ValidationMessages;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;

public class ConfirmPasswordUpdateFieldValidator extends PasswordUpdateFieldValidator {

    private static final ValidationMessages MSGS = GWT.create(ValidationMessages.class);

    private TextField<String>               passwordField;
    
    public ConfirmPasswordUpdateFieldValidator(TextField<String> confirmPasswordField, TextField<String> passwordField) {
        super(confirmPasswordField);
        this.passwordField = passwordField;

    }

    @Override
    public String validate(Field<?> field, String value)
    {

        String result = super.validate(field, value);
        if (result == null) {
            if (!value.trim().equals(passwordField.getValue())) {
                result = MSGS.passwordDoesNotMatch();
            }
        }
        return result;
    }
}
