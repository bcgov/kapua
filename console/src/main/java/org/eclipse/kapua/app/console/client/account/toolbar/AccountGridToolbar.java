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
package org.eclipse.kapua.app.console.client.account.toolbar;

import org.eclipse.kapua.app.console.client.ui.dialog.KapuaDialog;
import org.eclipse.kapua.app.console.client.ui.widget.EntityCRUDToolbar;
import org.eclipse.kapua.app.console.shared.model.GwtSession;
import org.eclipse.kapua.app.console.shared.model.account.GwtAccount;

import com.google.gwt.user.client.Element;


public class AccountGridToolbar extends EntityCRUDToolbar<GwtAccount> {

    public AccountGridToolbar(GwtSession currentSession) {
        super(currentSession);
    }
    
    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        super.getEditEntityButton().disable();
        super.getDeleteEntityButton().disable();
    }
    
    @Override
    protected KapuaDialog getAddDialog() {
        return new AccountAddDialog(currentSession);
    }
    
    @Override
    protected KapuaDialog getEditDialog() {
        GwtAccount selectedAccount = gridSelectionModel.getSelectedItem();
        AccountEditDialog dialog = null;
        if(selectedAccount != null){
            dialog = new AccountEditDialog(currentSession, selectedAccount);
        }
        return dialog;
    }
    
    @Override
    protected KapuaDialog getDeleteDialog() {
        GwtAccount selectedAccount = gridSelectionModel.getSelectedItem();
        AccountDeleteDialog dialog = null;
        if(selectedAccount != null){
            dialog = new AccountDeleteDialog(selectedAccount);
        }
        return dialog;
    }

}
