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

package org.openlmis.core.exceptions;


import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class LMISException extends Exception {

    public LMISException(String msg) {
        super(msg);
    }

    public LMISException(Exception e) {
        super(e);
    }

    public void reportToFabric() {
        //this will save exception messages locally
        //it only uploads to fabric server when network is available
        //so this actually behaves analogously with our sync data logic
        Crashlytics.logException(this);
        Log.e("Unexpected exception", this.getMessage(), this);
    }
}
