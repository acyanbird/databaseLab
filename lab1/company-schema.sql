DROP TABLE employee CASCADE;
CREATE TABLE employee (
  fname    varchar(15) not null, 
  minit    varchar(1),
  lname    varchar(15) not null,
  ssn      char(9),
  bdate    date,
  address  varchar(50),
  sex      char,
  salary   numeric(10,2),
  superssn char(9),
  dno      numeric(4),
  primary key (ssn),
  foreign key (superssn) references employee(ssn)
--  foreign key (dno) references department(dnumber)
);

DROP TABLE department CASCADE;
CREATE TABLE department (
  dname        varchar(25) not null,
  dnumber      numeric(4),
  mgrssn       char(9) not null, 
  mgrstartdate date,
  primary key (dnumber),
  unique (dname),
  foreign key (mgrssn) references employee(ssn)
);

ALTER TABLE employee ADD CONSTRAINT empDepFK foreign key (dno) references department(dnumber);

DROP TABLE dept_locations CASCADE;
CREATE TABLE dept_locations (
  dnumber   numeric(4),
  dlocation varchar(15), 
  primary key (dnumber,dlocation),
  foreign key (dnumber) references department(dnumber)
);

DROP TABLE project CASCADE;
CREATE TABLE project (
  pname      varchar(25) not null,
  pnumber    numeric(4),
  plocation  varchar(15),
  dnum       numeric(4) not null,
  primary key (pnumber),
  unique (pname),
  foreign key (dnum) references department(dnumber)
);

DROP TABLE works_on CASCADE;
CREATE TABLE works_on (
  essn   char(9),
  pno    numeric(4),
  hours  numeric(4,1),
  primary key (essn,pno),
  foreign key (essn) references employee(ssn),
  foreign key (pno) references project(pnumber)
);

DROP TABLE dependent CASCADE;
CREATE TABLE dependent (
  essn           char(9),
  dependent_name varchar(15),
  sex            char,
  bdate          date,
  relationship   varchar(8),
  primary key (essn,dependent_name),
  foreign key (essn) references employee(ssn)
);
