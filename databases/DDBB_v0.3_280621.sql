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
--  shema and just read on the public shema
GRANT chirpstack_user_cru TO chirpstack_user; -- operacion CRU en el schema app_chirpstack_user

-------------------------------------------------------
-- CREATE TABLES TO SHEMA app_chirpstack_user
---------------------------------------------------------

CREATE TABLE "app_chirpstack_user".company(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  company_name TEXT NOT NULL,
  NIF TEXT NOT NULL,
  address TEXT NOT NULL,
  code_postal TEXT NOT NULL,
  phone TEXT NOT NULL,
  email TEXT NOT NULL,
  web TEXT NOT NULL
);

CREATE TABLE "app_chirpstack_user".role(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  type_role TEXT NOT NULL,
  description TEXT,
  create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  update_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

CREATE TABLE "app_chirpstack_user".permission(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  type_permission TEXT NOT NULL,
  description TEXT,
  create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  update_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

CREATE TABLE "app_chirpstack_user".role_permission(
role_id UUID NOT NULL,
permission_id UUID NOT NULL,
create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
update_at TIMESTAMP WITH TIME ZONE DEFAULT NULL,
PRIMARY KEY (role_id, permission_id),

FOREIGN KEY (role_id) REFERENCES "app_chirpstack_user".role (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
FOREIGN KEY (permission_id) REFERENCES "app_chirpstack_user".permission(id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX idx_rp_role on "app_chirpstack_user".role_permission(role_id ASC);
CREATE INDEX idx_rp_permission on "app_chirpstack_user".role_permission(permission_id ASC);

CREATE TABLE "app_chirpstack_user".user(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL,
    company_id UUID NOT NULL,
    create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    name TEXT NOT NULL,
    nicknames TEXT NOT NULL,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY(company_id) REFERENCES "app_chirpstack_user".company(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (role_id)REFERENCES "app_chirpstack_user"."role" (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX idx_user_role "app_chirpstack_user".user(role_id ASC);

CREATE TABLE "app_chirpstack_user".session_up(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    company_id UUID NOT NULL,
    role_id UUID NOT NULL,
    email TEXT NOT NULL,
    FOREIGN KEY(company_id) REFERENCES "app_chirpstack_user".company(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".device(
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    company_id UUID NOT NULL,
    create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    device_name TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    FOREIGN KEY(company_id) REFERENCES "app_chirpstack_user".company(id) ON DELETE RESTRICT ON UPDATE CASCADE,
   FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

  CREATE TABLE "app_chirpstack_user".event( -- alert
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_name TEXT NOT NULL,
    conditional TEXT NOT NULL,
    conditional_Value FLOAT NOT NULL,
    currentValue FLOAT NOT NULL,
    email TEXT NOT NULL,
    create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE "app_chirpstack_user".notification( -- task
    id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    device_name TEXT NOT NULL,
    notification_name TEXT NOT NULL,
    condition TEXT NOT NULL,
    conditional_value FLOAT NOT NULL,
    email TEXT NOT NULL,
    FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE RESTRICT ON UPDATE CASCADE
); --

CREATE TABLE "app_chirpstack_user".incidence(
  id UUID PRIMARY KEY UNIQUE DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  company_id UUID NOT NULL,
  device_name TEXT NOT NULL,
  incidence_name TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  condition TEXT NOT NULL,
  value FLOAT NOT NULL,
  -- not duration bat will calculete time to recived notified and time actual
  FOREIGN KEY(user_id) REFERENCES "app_chirpstack_user".user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY(company_id) REFERENCES "app_chirpstack_user".company(id) ON DELETE RESTRICT ON UPDATE CASCADE

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





-- helpe drop

drop table
app_chirpstack_user.event,
app_chirpstack_user.company,
app_chirpstack_user.device,
app_chirpstack_user."permission",
app_chirpstack_user."role",
app_chirpstack_user.role_permission,
app_chirpstack_user.session_up,
app_chirpstack_user.notification,
app_chirpstack_user."user"
app_chirpstack_user."incidence"

