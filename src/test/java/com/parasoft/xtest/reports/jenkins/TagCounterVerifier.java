/*
 * Copyright 2018 Parasoft Corporation
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
package com.parasoft.xtest.reports.jenkins;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

class TagCounterVerifier
    extends DefaultHandler
{
    private final Map<String, Integer> _tagCountMap = new HashMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        Integer integer = _tagCountMap.get(qName);
        if (integer == null) {
            integer = 0;
        }
        _tagCountMap.put(qName, integer + 1);
    }

    int getNumber(String sName)
    {
        return _tagCountMap.get(sName) == null ? 0 : _tagCountMap.get(sName);
    }
}