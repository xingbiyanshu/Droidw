/*
 * Copyright (c) 2018 it.kedacom.com, Inc. All rights reserved.
 */

package com.sissi.droidw.utils;

import java.util.ArrayList;

/**
 * Created by Sissi on 12/15/2017.
 */

public class HanziToPinyinHelper {
    private static HanziToPinyin hanziToPinyin;
    private static final HanziToPinyinHelper ourInstance = new HanziToPinyinHelper();

    public static HanziToPinyinHelper getInstance() {
        return ourInstance;
    }

    private HanziToPinyinHelper() {
        hanziToPinyin = HanziToPinyin.getInstance();
    }

    public String getPinyin(String hanzi){
        ArrayList<HanziToPinyin.Token> tokens = hanziToPinyin.get(hanzi);
        StringBuffer pinyin = new StringBuffer();
        for (HanziToPinyin.Token token : tokens){
            pinyin.append(token.target);
        }
        return pinyin.toString();
    }

    public String getAcronym(String hanzi){
        ArrayList<HanziToPinyin.Token> tokens = hanziToPinyin.get(hanzi);
        StringBuffer acronym = new StringBuffer();
        for (HanziToPinyin.Token token : tokens){
            acronym.append(token.target.substring(0, 1));
        }
        return acronym.toString();
    }

    public BundleResult getBundleResult(String hanzi){
        ArrayList<HanziToPinyin.Token> tokens = hanziToPinyin.get(hanzi);
        StringBuffer pinyin = new StringBuffer();
        StringBuffer acronym = new StringBuffer();
        for (HanziToPinyin.Token token : tokens){
            pinyin.append(token.target);
            acronym.append(token.target.substring(0, 1));
        }

        return new BundleResult(pinyin.toString(), acronym.toString());
    }

    public static class BundleResult {
        private String pinyin;
        private String acronym;
        public BundleResult(String pinyin, String acronym){
            this.pinyin = pinyin;
            this.acronym = acronym;
        }

        public String getPinyin(){
            return pinyin;
        }

        public String getAcronym(){
            return acronym;
        }
    }
}
