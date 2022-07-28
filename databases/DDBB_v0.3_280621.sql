--------------------------------------
-- CREATE USERS
--------------------------------------
CREATE USER chirpstack_server WITH PASSWORD 'server3344';
CREATE USER chirpstack_user WITH PASSWORD 'user3344';

--------------------------------------
-- CREATE DATABASE
--------------------------------------
DROP DATABASE IF EXISTS chirpstack;
CREATE DATABASE chirpstack;

----------------------------------------------------------------
-- CREATE THE ROLE WITH ITS PERMISSIONS FOR THE "public" SCHEMA
  -- * create role  "public"
  -- * grant privileges connection to database this role
  -- * alter privileges to public schema and grant only insert
    -- privilege.
----------------------------------------------------------------
CREATE ROLE chirpstack_server_write;
GRANT CONNECT ON DATABASE chirpstack TO chirpstack_server_write;
GRANT INSERT ON ALL TABLES IN SCHEMA public TO chirpstack_server_write;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT INSERT ON TABLES TO chirpstack_server_write;

----------------------------------------------------------------
-- CREATE THE ROLE WITH ITS PERMISSIONS FOR THE chirpstack_user_cru SCHEMA
  -- * create role "chirpstack_user_cru"
  -- * grant privileges connection, usage and opetation CRUD to database this role
  -- * create sshema app_chirpstack_user
  -- * alter privileges to "public" schema and grant only select
    -- privilege.
  -- * alter privileges to "chirpstack_user_cru" schema and grant
    -- privilege CRUD operation.
----------------------------------------------------------------
-- create ROLE READ/WRITE to shema app_chirpstack_user
CREATE ROLE chirpstack_user_cru;
GRANT CONNECT ON DATABASE chirpstack TO chirpstack_user_cru;
CREATE SCHEMA app_chirpstack_user;

-- grant privileges to shemas
GRANT USAGE ON SCHEMA app_chirpstack_user TO chirpstack_user_cru;
GRANT USAGE ON SCHEMA public TO chirpstack_user_cru;

-- grant shema privileges for CRUD operation
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA app_chirpstack_user TO chirpstack_user_cru;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO chirpstack_user_cru;

ALTER DEFAULT PRIVILEGES IN SCHEMA app_chirpstack_user GRANT SELECT, INSERT, UPDATE ON TABLES TO chirpstack_user_cru;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO chirpstack_user_cru;

-- grant shema previleges usage on sequences
GRANT USAGE ON ALL SEQUENCES IN SCHEMA app_chirpstack_user TO chirpstack_user_cru;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO chirpstack_user_cru;

ALTER DEFAULT PRIVILEGES IN SCHEMA app_chirpstack_user GRANT USAGE ON SEQUENCES TO chirpstack_user_cru;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO chirpstack_user_cru;
-----------------------------------------------------
-- GRANT USERS SCHEMA USAGE PRIVILEGES
-----------------------------------------------------
-- this user will only insert data for public shema
GRANT chirpstack_server_write TO chirpstack_server;
-- this user can do CRUD operation on the app_chirpstack_server
--  schema and just read on the public schema
GRANT chirpstack_user_cru TO chirpstack_user; -- operacion CRU en el schema app_chirpstack_user

-------------------------------------------------------
-- CREATE TABLES TO SHEMA app_chirpstack_user
---------------------------------------------------------

CREATE TABLE "app_chirpstack_user".company(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  owner_id UUID UNIQUE NOT NULL , -- ADMIN / para el mismo id a la hora de crear el usuario en la tabla user.
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  name TEXT NOT NULL
);

INSERT INTO "app_chirpstack_user".company ( id, owner_id, name) VALUES (
  'd4c9a0ce-ea21-8fab-8fb0-9ce64ff4a0cc', 'd4cb673d-8f75-835d-d6a5-54964bca1d50', 'Gantabi OneClieck'
);

INSERT INTO "app_chirpstack_user".company ( id, owner_id, name) VALUES (
  'd4cba54a-da8f-915a-9165-fdaad428821d', 'd4cc2c37-afca-91bf-f6ce-43d0cdce0715', 'Aldi Comer'
);

INSERT INTO "app_chirpstack_user".company ( id, owner_id, name) VALUES (
  'd4ebaa6b-810c-7973-c655-a3d099bf4fe1', 'd4cf5fa7-e8d7-e937-ae9c-442cdbd59b1f', 'My Company'
);


CREATE TABLE "app_chirpstack_user".user(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    company_id UUID DEFAULT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT NULL,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
   FOREIGN KEY (company_id)REFERENCES "app_chirpstack_user".company (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".admin(
  id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  last_seen TIMESTAMP WITH TIME ZONE DEFAULT NULL, -- por defecto null
  super_admin BOOLEAN DEFAULT NULL,
  name TEXT NOT NULL,
  surname TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL
);
CREATE UNIQUE INDEX i_true_admin ON  "app_chirpstack_user".admin(id, (super_admin IS TRUE));

CREATE TABLE "app_chirpstack_user".admin_company(
  admin_id UUID NOT NULL,
  company_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

  PRIMARY KEY (admin_id, company_id),

  FOREIGN KEY (admin_id)REFERENCES "app_chirpstack_user".admin (id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (company_id)REFERENCES "app_chirpstack_user".company (id) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO "app_chirpstack_user".admin_company (admin_id, company_id) VALUES (
  'd4e41470-93ea-9e88-14a8-7ddaca478e49', 'd4c9a0ce-ea21-8fab-8fb0-9ce64ff4a0cc'
);
INSERT INTO "app_chirpstack_user".admin_company (admin_id, company_id) VALUES (
  'd4e41470-93ea-9e88-14a8-7ddaca478e49', 'd4cba54a-da8f-915a-9165-fdaad428821d'
);

INSERT INTO "app_chirpstack_user".admin_company (admin_id, company_id) VALUES (
  'd4e50e8a-8684-2269-cb56-a555a5de8f5e', 'd4ebaa6b-810c-7973-c655-a3d099bf4fe1'
);


CREATE TABLE "app_chirpstack_user".app(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  external_id INTEGER UNIQUE NOT NULL ,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  name TEXT NOT NULL
);

CREATE TABLE "app_chirpstack_user".company_app(
  app_id UUID NOT NULL,
  company_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (app_id, company_id),
  FOREIGN KEY (app_id)REFERENCES "app_chirpstack_user".app(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (company_id)REFERENCES "app_chirpstack_user".company(id) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE "app_chirpstack_user".session_up2(
    id UUID,
    user_id UUID NOT NULL,
    company_id UUID NOT NULL,
   token TEXT NOT NULL,
    PRIMARY KEY (id, user_id),
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".password(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    url TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".device(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    name TEXT NOT NULL,
    serial TEXT NOT NULL UNIQUE,
    FOREIGN KEY(app_id) REFERENCES "app_chirpstack_user".app(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".user_device(
    user_id UUID NOT NULL,
    device_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY(device_id) REFERENCES "app_chirpstack_user".device(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE CASCADE ON UPDATE CASCADE

);

CREATE TABLE "app_chirpstack_user".event(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    event_name TEXT NOT NULL,
    device_name TEXT NOT NULL,
    conditional TEXT NOT NULL,
    conditional_Value FLOAT NOT NULL,
    currentValue FLOAT NOT NULL,
    FOREIGN KEY(device_id) REFERENCES "app_chirpstack_user".device(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".notification(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    notification_name TEXT NOT NULL,
    device_name TEXT NOT NULL,
    type TEXT NOT NULL,
    condition TEXT NOT NULL,
    value_conditional FLOAT NOT NULL,
    send_mode TEXT NOT NULL,
    addressee TEXT NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    FOREIGN KEY(device_id) REFERENCES "app_chirpstack_user".device(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE CASCADE ON UPDATE CASCADE
); --

CREATE TABLE "app_chirpstack_user".incidence(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  device_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  device_name TEXT NOT NULL,
  incidence_name TEXT NOT NULL,
  condition TEXT NOT NULL,
  value_conditional FLOAT NOT NULL,
  value FLOAT NOT NULL,
  FOREIGN KEY(device_id) REFERENCES "app_chirpstack_user".device(id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE CASCADE ON UPDATE CASCADE
);
-----------------------------------------------------------
-- CREATE TABLES TO SHEMA public
-----------------------------------------------------------
-- creamos la tablas para el schema  sch_hirpStack_server--
--/ Tablas obligatoria que recogen los datos de los dispositivos de Servidor [ChirpStack Server] /
-- CREACION DE TABLAS

CREATE TABLE "public".device_up (
  id UUID PRIMARY KEY,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL,
  dev_eui BYTEA NOT NULL,
  device_name TEXT NOT NULL,
  application_id BIGINT NOT NULL,
  application_name TEXT NOT NULL,
  frequency BIGINT NOT NULL,
  dr SMALLINT NOT NULL,
  adr BOOLEAN NOT NULL,
  f_cnt BIGINT NOT NULL,
  f_port SMALLINT NOT NULL,
  tags HSTORE NOT NULL,
  data BYTEA NOT NULL,
  rx_info JSONB NOT NULL,
  object JSONB NOT NULL
);

CREATE INDEX idx_device_up_received_at ON "public".device_up(received_at);
CREATE INDEX idx_device_up_dev_eui ON "public".device_up(dev_eui);
CREATE INDEX idx_device_up_application_id ON "public".device_up(application_id);
CREATE INDEX idx_device_up_frequency ON "public".device_up(frequency);
CREATE INDEX idx_device_up_dr ON "public".device_up(dr);
CREATE INDEX idx_device_up_tags ON "public".device_up(tags);

CREATE TABLE "public".device_status (
  id UUID PRIMARY KEY,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL,
  dev_eui BYTEA NOT NULL,
  device_name TEXT NOT NULL,
  application_id BIGINT NOT NULL,
  application_name TEXT NOT NULL,
  margin SMALLINT NOT NULL,
  external_power_source BOOLEAN NOT NULL,
  battery_level_unavailable BOOLEAN NOT NULL,
  battery_level NUMERIC(5, 2) NOT NULL,
  tags HSTORE NOT NULL
);

CREATE INDEX idx_device_status_received_at ON "public".device_status(received_at);
CREATE INDEX idx_device_status_dev_eui ON "public".device_status(dev_eui);
CREATE INDEX idx_device_status_application_id ON "public".device_status(application_id);
CREATE INDEX idx_device_status_tags ON "public".device_status(tags);

CREATE TABLE "public".device_join (
  id UUID PRIMARY KEY,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL,
  dev_eui BYTEA NOT NULL,
  device_name TEXT NOT NULL,
  application_id bigint NOT NULL,
  application_name TEXT NOT NULL,
  dev_addr BYTEA NOT NULL,
  tags HSTORE NOT NULL
);

CREATE INDEX idx_device_join_received_at ON "public".device_join(received_at);
CREATE INDEX idx_device_join_dev_eui ON "public".device_join(dev_eui);
CREATE INDEX idx_device_join_application_id ON "public".device_join(application_id);
CREATE INDEX idx_device_join_tags ON "public".device_join(tags);

CREATE TABLE "public".device_ack (
  id UUID PRIMARY KEY,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL,
  dev_eui BYTEA NOT NULL,
  device_name TEXT NOT NULL,
  application_id BIGINT NOT NULL,
  application_name TEXT NOT NULL,
  acknowledged BOOLEAN NOT NULL,
  f_cnt BIGINT NOT NULL,
  tags HSTORE NOT NULL
);

CREATE INDEX idx_device_ack_received_at ON "public".device_ack(received_at);
CREATE INDEX idx_device_ack_dev_eui ON "public".device_ack(dev_eui);
CREATE INDEX idx_device_ack_application_id ON "public".device_ack(application_id);
CREATE INDEX idx_device_ack_tags ON "public".device_ack(tags);

CREATE TABLE "public".device_error (
  id UUID PRIMARY KEY,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL,
  dev_eui BYTEA NOT NULL,
  device_name TEXT NOT NULL,
  application_id BIGINT NOT NULL,
  application_name TEXT NOT NULL,
  type TEXT NOT NULL,
  error TEXT NOT NULL,
  f_cnt BIGINT NOT NULL,
  tags HSTORE NOT NULL
);

CREATE INDEX idx_device_error_received_at ON "public".device_error(received_at);
CREATE INDEX idx_device_error_dev_eui ON "public".device_error(dev_eui);
CREATE INDEX idx_device_error_application_id ON "public".device_error(application_id);
CREATE INDEX idx_device_error_tags ON "public".device_error(tags);

CREATE TABLE "public".device_location (
  id UUID PRIMARY KEY,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL,
  dev_eui BYTEA NOT NULL,
  device_name TEXT NOT NULL,
  application_id BIGINT NOT NULL,
  application_name TEXT NOT NULL,
  altitude DOUBLE PRECISION NOT NULL,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  geohash TEXT NOT NULL,
  tags hstore NOT NULL,
  accuracy SMALLINT NOT NULL
);

CREATE INDEX idx_device_location_received_at ON "public".device_location(received_at);
CREATE INDEX idx_device_location_dev_eui ON "public".device_location(dev_eui);
CREATE INDEX idx_device_location_application_id ON "public".device_location(application_id);
CREATE INDEX idx_device_location_tags ON "public".device_location(tags);

