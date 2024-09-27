/*
 * Copyright 2017 Parasoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parasoft.findings.jenkins;

import java.util.Locale;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;


public class LocalizableString // parasoft-suppress OWASP2021.A8.OROM "reviewed"
    extends Localizable
{
    private static final long serialVersionUID = 5885639086662718677L;

    private String _sMessage = null;

    public LocalizableString(ResourceBundleHolder holder, String key, Object... args)
    {
        super(holder, key, args);
    }

    public LocalizableString(String message)
    {
        super(null, null);
        _sMessage = message;
    }

    @Override
    public String toString()
    {
        if (_sMessage != null) {
            return _sMessage;
        }
        return super.toString();
    }

    @Override
    public String toString(Locale locale)
    {
        if (_sMessage != null) {
            return _sMessage;
        }
        return super.toString(locale);
    }

}
