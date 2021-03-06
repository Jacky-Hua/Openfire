-- $Revision$
-- $Date$

INSERT INTO ofVersion (name, version) VALUES ('clientcontrol', 0);

CREATE TABLE ofBookmark (
   bookmarkID       INTEGER          NOT NULL,
   bookmarkType     VARCHAR(50)      NOT NULL,
   bookmarkName     VARCHAR(255)     NOT NULL,
   bookmarkValue    VARCHAR(1024)    NOT NULL,
   isGlobal         INTEGER          NOT NULL,
   CONSTRAINT ofBookmark_pk PRIMARY KEY (bookmarkID)
);

CREATE TABLE ofBookmarkPerm (
   bookmarkID   INTEGER              NOT NULL,
   bookmarkType SMALLINT             NOT NULL,
   name         VARCHAR(255)         NOT NULL,
   CONSTRAINT ofBookmarkPerm_pk PRIMARY KEY(bookmarkID, name, bookmarkType)
);

CREATE TABLE ofBookmarkProp (
   bookmarkID   INTEGER              NOT NULL,
   name         VARCHAR(100)         NOT NULL,
   propValue    TEXT                 NOT NULL,
   CONSTRAINT ofBookmarkProp_pk PRIMARY KEY (bookmarkID, name)
);
