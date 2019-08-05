package com.apollo.kalampich.model;

/**
 * Created by cpu on 1/15/2018.
 */

public class ChapterItem {

        private String title;
        private Integer code;

        public ChapterItem() {
            super();
        }

        public void setCode(Integer CatCode) {
            this.code = CatCode;
        }
        public Integer getCode() {
            return code;
        }

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

}
