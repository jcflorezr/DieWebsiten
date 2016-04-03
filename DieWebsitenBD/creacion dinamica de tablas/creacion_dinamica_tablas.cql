/*
- revisar los PENDIENTE!!!!! en los .csv
	1. cuando las columnas son de tipo coleccion deben llevar este formato: ej: "{'list':['varchar']}"  - "{'set':['varchar']}"  -  "{'map':['varchar', 'varchar']}"
	2. crear validacion especial para la columna 'tipo_validador' -$
	3. las COLMNAS_RECUPERADAS a parte de la validacion que tienen, necesitan una segunda validacion la cual es el query de la columna de referencia
	4. crear validacion para caracteres separados por algun caracter especial, bien sea , . :@: etc
	5. proteger las COLUMNAS_POBLADAS de escritura desde el cliente

*/



DROP KEYSPACE IF EXISTS DIEWEBSITEN;

CREATE KEYSPACE IF NOT EXISTS DIEWEBSITEN WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 2};

USE DIEWEBSITEN;

CREATE TABLE TABLAS (
	KEYSPACE_NAME VARCHAR,
	COLUMNFAMILY_NAME VARCHAR,
	PARENT_COLUMNFAMILY_NAME VARCHAR,
	TRANSACCIONES_POR_TABLA LIST<VARCHAR>,
	PRIMARY KEY (KEYSPACE_NAME, COLUMNFAMILY_NAME)
);

CREATE TABLE TIPOS_DE_TRANSACCIONES (
	TIPO_TRANSACCION VARCHAR,
	DESCRIPCION VARCHAR,
	PRIMARY KEY (TIPO_TRANSACCION)
);

CREATE TABLE TIPOS_DE_DATOS (
	CQL_TYPE VARCHAR,
	INICIAL VARCHAR,
	VALIDADOR VARCHAR,
	DESCRIPCION VARCHAR,
	PRIMARY KEY (CQL_TYPE, INICIAL, VALIDADOR)
);

CREATE TABLE TIPOS_DE_COLUMNAS (
	TIPO_COLUMNA VARCHAR,
	IDENTIFICADOR VARCHAR,
	DESCRIPCION VARCHAR,
	PRIMARY KEY (TIPO_COLUMNA)
);

CREATE TABLE COLUMNAS (
	KEYSPACE_NAME VARCHAR,
	COLUMNFAMILY_NAME VARCHAR,
	COLUMN_NAME VARCHAR,	
	GRUPO_VALIDACION VARCHAR,
	CQL_TYPE VARCHAR,
	TIPOS_COLUMNA LIST<VARCHAR>,
	COLUMNA_REFERENCIA VARCHAR,
	QUERY_COLUMNA_REFERENCIA VARCHAR,
	PRIMARY KEY (KEYSPACE_NAME, COLUMNFAMILY_NAME, COLUMN_NAME)
);

CREATE TABLE GRUPOS_DE_VALIDACIONES (
	GRUPO_VALIDACION VARCHAR,
	TIPO VARCHAR,
	VALIDACION VARCHAR,		
	PRIMARY KEY (GRUPO_VALIDACION, TIPO, VALIDACION)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}' AND
CLUSTERING ORDER BY (TIPO DESC);

CREATE TABLE VALIDACIONES (
	TIPO VARCHAR,
	VALIDACION VARCHAR,	
	DESCRIPCION VARCHAR,
	MENSAJE_RESPUESTA VARCHAR,
	PRIMARY KEY (TIPO, VALIDACION)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

/*COPY diewebsiten.colecciones (tipo, coleccion, tipolist, tiposet, tipomap) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_colecciones.csv';*/

COPY diewebsiten.tablas (keyspace_name, columnfamily_name, parent_columnfamily_name, TRANSACCIONES_POR_TABLA) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_tablas.csv';
COPY diewebsiten.TIPOS_DE_TRANSACCIONES (tipo_transaccion, descripcion) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_tipos_de_transacciones.csv';
COPY diewebsiten.TIPOS_DE_DATOS (cql_type, inicial, validador, descripcion) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_tipos_de_datos.csv';
COPY diewebsiten.TIPOS_DE_COLUMNAS (tipo_columna, identificador, descripcion) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_tipos_de_columnas.csv';
COPY diewebsiten.columnas (keyspace_name, columnfamily_name, column_name, grupo_validacion, cql_type, tipos_columna, columna_referencia, query_columna_referencia) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_columnas.csv';
COPY diewebsiten.grupos_de_validaciones (grupo_validacion, tipo, validacion) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_grupos_validaciones.csv';
COPY diewebsiten.validaciones (tipo, validacion, descripcion) FROM '~/DieWebsiten/DieWebsitenBD/creacion dinamica de tablas/cargar_validaciones.csv';