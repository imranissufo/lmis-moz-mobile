/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.DateUtil;

import lombok.Data;

@Data
public class RnRFormViewModel {

    public static final int TYPE_GROUP = 0;
    public static final int TYPE_DRAFT = 1;
    public static final int TYPE_UNSYNC = 2;
    public static final int TYPE_HISTORICAL = 3;

    int type;
    String syncedDate;
    String period;
    String title;
    String name;
    long id;

    public RnRFormViewModel(RnRForm form){
        this.syncedDate = DateUtil.formatDate(form.getUpdatedAt());
        this.period = DateUtil.formatDate(form.getPeriodBegin()) + " to " + DateUtil.formatDate(form.getPeriodEnd());
        this.name = form.getProgram().getProgramName();
        this.id = form.getId();

        if (form.getStatus() != RnRForm.STATUS.AUTHORIZED){
            this.type = TYPE_DRAFT;
        } else if (!form.isSynced()){
            this.type = TYPE_UNSYNC;
        } else {
            this.type = TYPE_HISTORICAL;
        }
    }

    public RnRFormViewModel(String title){
        this.type = TYPE_GROUP;
        this.title = title;
    }
}
