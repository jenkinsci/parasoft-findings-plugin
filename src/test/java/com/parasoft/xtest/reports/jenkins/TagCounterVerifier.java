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