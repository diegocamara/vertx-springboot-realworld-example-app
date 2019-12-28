CREATE TABLE USERS(
  ID VARCHAR(255) PRIMARY KEY,
  USERNAME VARCHAR(255),
  BIO VARCHAR(255),
  EMAIL VARCHAR(255),
  IMAGE VARCHAR(255),
  PASSWORD VARCHAR(255),
  TOKEN VARCHAR(500)
);

CREATE TABLE FOLLOWED_USERS(
  USER_ID VARCHAR(255) NOT NULL,
  FOLLOWED_ID VARCHAR(255) NOT NULL,
  PRIMARY KEY (USER_ID, FOLLOWED_ID),
  CONSTRAINT FK_USER_ID FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
  CONSTRAINT FK_FOLLOWED_ID FOREIGN KEY (FOLLOWED_ID) REFERENCES USERS(ID)
);

CREATE TABLE ARTICLES(
  ID VARCHAR(255) PRIMARY KEY,
  TITLE VARCHAR(255) NOT NULL,
  DESCRIPTION VARCHAR(255),
  BODY VARCHAR(255),
  AUTHOR_ID VARCHAR(255) NOT NULL,
  CREATED_AT DATE,
  UPDATED_AT DATE,
  CONSTRAINT FK_AUTHOR_ID FOREIGN KEY (AUTHOR_ID) REFERENCES USERS(ID)
);