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
package org.eclipse.kapua.service.authorization.shiro;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authorization.permission.Actions;

public class CucRole {

    private String name = null;
    private Integer scopeId = null;
    private String actions = null;
    private KapuaId id = null;
    private Set<Actions> actionSet = null;

    public void doParse() {
        if (this.scopeId != null) {
            this.id = new KapuaEid(BigInteger.valueOf(scopeId));
        }
        if (this.actions != null) {
            String tmpAct = this.actions.trim().toLowerCase();
            if (tmpAct.length() != 0) {
                this.actionSet = new HashSet<>();
                String[] tmpList = this.actions.split(",");

                for (String tmpS : tmpList) {
                    switch (tmpS.trim().toLowerCase()) {
                    case "read":
                        this.actionSet.add(Actions.read);
                        break;
                    case "write":
                        this.actionSet.add(Actions.write);
                        break;
                    case "delete":
                        this.actionSet.add(Actions.delete);
                        break;
                    case "connect":
                        this.actionSet.add(Actions.connect);
                        break;
                    case "execute":
                        this.actionSet.add(Actions.execute);
                        break;
                    }
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScopeId(KapuaId id) {
        this.id = id;
    }

    public KapuaId getScopeId() {
        return this.id;
    }

    public Set<Actions> getActions() {
        return actionSet;
    }

    public void setActions(Set<Actions> actions) {
        this.actionSet = actions;
    }
}
