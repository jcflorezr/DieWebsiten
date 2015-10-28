/* Los campos de las sentencias CQL que se guardan en el campo 'diewebsiten.eventos.sentenciacql' deben ir en orden alfabetico */
/* Se debe ejecutar script que modifica los comentarios de las tablas de las bases de datos del sistema (system, system_traces)*/

DROP KEYSPACE IF EXISTS DIEWEBSITEN;

CREATE KEYSPACE IF NOT EXISTS DIEWEBSITEN WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 2};

CREATE TABLE DIEWEBSITEN.COLUMNAS (
	KEYSPACE_NAME VARCHAR,
	COLUMNFAMILY_NAME VARCHAR,
	COLUMN_NAME VARCHAR,	
	GRUPOVALIDACION VARCHAR,
	PRIMARY KEY (KEYSPACE_NAME, COLUMNFAMILY_NAME, COLUMN_NAME)
) WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.TRANSACCIONES_POR_TABLA (
	KEYSPACE_NAME VARCHAR,
	COLUMNFAMILY_NAME VARCHAR,
	TIPOTRANSACCION VARCHAR,
	PRIMARY KEY (KEYSPACE_NAME, COLUMNFAMILY_NAME)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

/*CREATE TABLE DIEWEBSITEN.SITIOSWEB_PAGINAS_ELEMENTOS (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,
	TIPO VARCHAR,
	NOMBRE VARCHAR,
	DESCRIPCION VARCHAR,
	BASESDEDATOS SET<VARCHAR>,
	PRIMARY KEY (TIPO, SITIOWEB, PAGINA)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';*/

CREATE TABLE DIEWEBSITEN.SITIOSWEB (
	SITIOWEB VARCHAR,
	NOMBRE VARCHAR,
	DESCRIPCION VARCHAR,
	KEYSPACES LIST<VARCHAR>,
	PRIMARY KEY (SITIOWEB)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.PAGINAS (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,
	NOMBRE VARCHAR,
	DESCRIPCION VARCHAR,
	PRIMARY KEY (SITIOWEB, PAGINA)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.EVENTOS (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,	
	EVENTO VARCHAR,
	TRANSACCION VARCHAR,
	TIPOTRANSACCION VARCHAR,
	KEYSPACE_NAME VARCHAR,
	COLUMNFAMILY_NAME VARCHAR,
	ORDEN INT,
	DESCRIPCION VARCHAR,
	SENTENCIACQL VARCHAR,
	FILTROSSENTENCIACQL LIST<VARCHAR>,	
	COLUMNASINTERMEDIASSENTENCIACQL LIST<VARCHAR>,
	COLUMNASCONSULTASENTENCIACQL LIST<VARCHAR>,
	--NECESITAFORMULARIO VARCHAR,
	--TIPOLISTA VARCHAR,
	PRIMARY KEY (SITIOWEB, PAGINA, EVENTO, TRANSACCION/*, TIPOTRANSACCION, BASEDEDATOS*/)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.FORMULARIOS (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,
	EVENTO VARCHAR,
	KEYSPACE_NAME VARCHAR,
	COLUMNFAMILY_NAME VARCHAR,
	COLUMN_NAME VARCHAR,	
	GRUPOVALIDACION VARCHAR,
	FORMAINGRESO VARCHAR,
	VALORPORDEFECTO VARCHAR,
	PRIMARY KEY (SITIOWEB, PAGINA, EVENTO, KEYSPACE_NAME, COLUMNFAMILY_NAME, COLUMN_NAME)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

/*CREATE TABLE DIEWEBSITEN.SENTENCIAS_CQL (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,
	TIPOTRANSACCION VARCHAR,
	TRANSACCION VARCHAR,
	CLAUSULA VARCHAR,
	CAMPO VARCHAR,
	ALIAS VARCHAR,
	VALORPORDEFECTO VARCHAR,
	PRIMARY KEY (SITIOWEB, PAGINA, TIPOTRANSACCION, TRANSACCION, CLAUSULA, CAMPO)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';*/

CREATE TABLE DIEWEBSITEN.GRUPOS_VALIDACIONES (
	GRUPO VARCHAR,
	TIPO VARCHAR,
	VALIDACION VARCHAR,		
	PRIMARY KEY (GRUPO, TIPO, VALIDACION)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}' AND
CLUSTERING ORDER BY (TIPO DESC);

CREATE TABLE DIEWEBSITEN.VALIDACIONES (
	TIPO VARCHAR,
	VALIDACION VARCHAR,	
	DESCRIPCION VARCHAR,
	PRIMARY KEY (TIPO, VALIDACION)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

/*CREATE TABLE DIEWEBSITEN.USUARIOS (
	SITIOWEB VARCHAR,	
	DOMINIO VARCHAR,
	USUARIO VARCHAR,
	CONTRASENA VARCHAR,
	PRIMARY KEY (SITIOWEB, DOMINIO, USUARIO)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.SESIONES (
	SITIOWEB VARCHAR,
	DOMINIO VARCHAR,
	USUARIO VARCHAR,
	SESSIONID VARCHAR,
	FECHAULTIMOACCESO TIMESTAMP,
	PRIMARY KEY (SITIOWEB, DOMINIO, USUARIO, SESSIONID)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';
  
CREATE TABLE DIEWEBSITEN.COLECCIONES (
	TIPO VARCHAR,
	COLECCION VARCHAR,		
	TIPOLIST LIST<VARCHAR>,
	TIPOSET SET<VARCHAR>,
	TIPOMAP MAP<VARCHAR, VARCHAR>,
	PRIMARY KEY (TIPO, COLECCION)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.CODIGOS_POR_PAGINA (
	SITIOWEB VARCHAR,
	IDIOMA VARCHAR,
	PAGINA VARCHAR,
    NOMBRE VARCHAR,	
	CODIGO VARCHAR,
	PRIMARY KEY (SITIOWEB, IDIOMA, PAGINA, NOMBRE)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.DESPLIEGUES (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,	
	RUTA VARCHAR,
	PRIMARY KEY (SITIOWEB, PAGINA, RUTA)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';

CREATE TABLE DIEWEBSITEN.ENUNCIADOS (
	SITIOWEB VARCHAR,
	PAGINA VARCHAR,
	NIVEL INT,
	IDIOMA VARCHAR,	
	ETIQUETA VARCHAR,
	RUTA VARCHAR,
	ATRIBUTOS MAP<VARCHAR, VARCHAR>,
	TEXTO VARCHAR,
	PRIMARY KEY (SITIOWEB, PAGINA, IDIOMA, NIVEL, ETIQUETA, RUTA)
)WITH comment = '{"transacciones" : ["SELECT", "INSERT", "UPDATE", "DELETE"], "comentario" : ""}';*/

/*COPY diewebsiten.transacciones_por_tabla (tipotransaccion, basededatos, tabla) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_transacciones_por_tabla.csv';
COPY diewebsiten.diccionario_columnas (basededatos, tabla, columna, grupovalidacion) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_diccionario_columnas.csv';
COPY diewebsiten.sitiosweb_paginas_elementos (sitioweb, pagina, tipo, nombre, descripcion, basesdedatos) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_sitiosweb_paginas_elementos.csv';
COPY diewebsiten.despliegues (sitioweb, pagina, ruta) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_despliegues.csv';
COPY diewebsiten.codigos_por_pagina (sitioweb, idioma, pagina, nombre, codigo) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_codigos_por_pagina.csv';
COPY diewebsiten.enunciados (sitioweb, pagina, idioma, nivel, etiqueta, ruta, atributos, texto) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_enunciados.csv';
COPY diewebsiten.eventos (sitioweb, pagina, evento, transaccion, tipotransaccion, basededatos, tabla, orden, descripcion, sentenciacql, necesitaformulario, tipolista) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_eventos.csv';
COPY diewebsiten.grupos_validaciones (grupo, validaciones, transformaciones) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_grupos_validaciones.csv';
--COPY diewebsiten.sentencias_cql (sitioweb, pagina, tipotransaccion, transaccion, clausula, campo, alias, valorpordefecto) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_sentencias_cql.csv';
COPY diewebsiten.formularios (sitioweb, pagina, evento, basededatos, tabla, columna, grupovalidacion, formaingreso) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_formularios.csv';
COPY diewebsiten.validaciones (tipo, nombre, descripcion) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_validaciones.csv';
COPY diewebsiten.usuarios (sitioweb, usuario, dominio, contrasena) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_usuarios.csv';
COPY diewebsiten.sesiones (sitioweb, usuario, dominio, sessionid, fechaultimoacceso) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_sesiones.csv';
COPY diewebsiten.colecciones (tipo, coleccion, tipolist, tiposet, tipomap) FROM 'C:\Users\jflorezr\Documents\DW\DieWebsitenBD\cargar_colecciones.csv';*/

COPY diewebsiten.transacciones_por_tabla (keyspace_name, columnfamily_name, tipotransaccion) FROM '~/DieWebsiten/DieWebsitenBD/cargar_transacciones_por_tabla.csv';
COPY diewebsiten.columnas (keyspace_name, columnfamily_name, column_name, grupovalidacion) FROM '~/DieWebsiten/DieWebsitenBD/cargar_columnas.csv';
COPY diewebsiten.sitiosweb (sitioweb, nombre, descripcion, keyspaces) FROM '~/DieWebsiten/DieWebsitenBD/cargar_sitiosweb.csv';
COPY diewebsiten.paginas (sitioweb, pagina, nombre, descripcion) FROM '~/DieWebsiten/DieWebsitenBD/cargar_paginas.csv';
--COPY diewebsiten.despliegues (sitioweb, pagina, ruta) FROM '~/DieWebsiten/DieWebsitenBD/cargar_despliegues.csv';
--COPY diewebsiten.codigos_por_pagina (sitioweb, idioma, pagina, nombre, codigo) FROM '~/DieWebsiten/DieWebsitenBD/cargar_codigos_por_pagina.csv';
--COPY diewebsiten.enunciados (sitioweb, pagina, idioma, nivel, etiqueta, ruta, atributos, texto) FROM '~/DieWebsiten/DieWebsitenBD/cargar_enunciados.csv';
COPY diewebsiten.eventos (sitioweb, pagina, evento, transaccion, tipotransaccion, keyspace_name, columnfamily_name, orden, descripcion, sentenciacql, filtrossentenciacql, columnasintermediassentenciacql, columnasconsultasentenciacql) FROM '~/DieWebsiten/DieWebsitenBD/cargar_eventos.csv';
COPY diewebsiten.grupos_validaciones (grupo, tipo, validacion) FROM '~/DieWebsiten/DieWebsitenBD/cargar_grupos_validaciones.csv';
--COPY diewebsiten.sentencias_cql (sitioweb, pagina, tipotransaccion, transaccion, clausula, campo, alias, valorpordefecto) FROM '~/DieWebsiten/DieWebsitenBD/cargar_sentencias_cql.csv';
COPY diewebsiten.formularios (sitioweb, pagina, evento, keyspace_name, columnfamily_name, column_name, grupovalidacion, formaingreso, valorpordefecto) FROM '~/DieWebsiten/DieWebsitenBD/cargar_formularios.csv';
COPY diewebsiten.validaciones (tipo, validacion, descripcion) FROM '~/DieWebsiten/DieWebsitenBD/cargar_validaciones.csv';
--COPY diewebsiten.usuarios (sitioweb, usuario, dominio, contrasena) FROM '~/DieWebsiten/DieWebsitenBD/cargar_usuarios.csv';
--COPY diewebsiten.sesiones (sitioweb, usuario, dominio, sessionid, fechaultimoacceso) FROM '~/DieWebsiten/DieWebsitenBD/cargar_sesiones.csv';
--COPY diewebsiten.colecciones (tipo, coleccion, tipolist, tiposet, tipomap) FROM '~/DieWebsiten/DieWebsitenBD/cargar_colecciones.csv';
