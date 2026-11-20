-- Clínica Guadalupe: datos iniciales de desarrollo.
-- Contraseña para TODAS las cuentas: 123456789
-- El script se puede ejecutar más de una vez: no duplica cuentas, perfiles,
-- especialidades, consultorios ni horarios.

USE clinica_guadalupe;

SET @clave_hash = '$2a$10$iV5pqMcvaGMJu9VjgAfSm.au11Zi0FO3s4VvXwD/kYYTFyOI1GIae';

START TRANSACTION;

-- 1) Especialidades
INSERT INTO especialidades (activo, descripcion, fecha_actualizacion, fecha_creacion, nombre)
SELECT 1, datos.descripcion, NOW(), NOW(), datos.nombre
FROM (
    SELECT 'Cardiología' AS nombre, 'Prevención, diagnóstico y tratamiento cardiovascular.' AS descripcion
    UNION ALL SELECT 'Pediatría', 'Atención médica integral para niños y adolescentes.'
    UNION ALL SELECT 'Ginecología', 'Salud integral de la mujer y atención ginecológica.'
    UNION ALL SELECT 'Obstetricia', 'Control del embarazo, parto y puerperio.'
    UNION ALL SELECT 'Medicina interna', 'Diagnóstico y tratamiento integral del paciente adulto.'
    UNION ALL SELECT 'Medicina general', 'Atención primaria, prevención y seguimiento clínico.'
    UNION ALL SELECT 'Oftalmología', 'Evaluación y tratamiento de enfermedades oculares.'
    UNION ALL SELECT 'Dermatología', 'Diagnóstico y tratamiento de enfermedades de la piel.'
    UNION ALL SELECT 'Traumatología', 'Atención de lesiones musculoesqueléticas.'
    UNION ALL SELECT 'Neurología', 'Diagnóstico y tratamiento del sistema nervioso.'
    UNION ALL SELECT 'Endocrinología', 'Atención de trastornos hormonales y metabólicos.'
    UNION ALL SELECT 'Gastroenterología', 'Atención de enfermedades del aparato digestivo.'
    UNION ALL SELECT 'Urología', 'Salud del aparato urinario y reproductor masculino.'
    UNION ALL SELECT 'Otorrinolaringología', 'Atención de oído, nariz y garganta.'
    UNION ALL SELECT 'Psiquiatría', 'Evaluación y tratamiento de la salud mental.'
) AS datos
WHERE NOT EXISTS (
    SELECT 1 FROM especialidades e WHERE LOWER(e.nombre) = LOWER(datos.nombre)
);

-- 2) Cuentas de acceso: administrador, recepcionista, pacientes y médicos.
INSERT INTO usuarios
    (activo, apellidos, dni, email, fecha_actualizacion, fecha_creacion,
     nombres, password, rol, telefono)
SELECT datos.activo, datos.apellidos, datos.dni, datos.email, NOW(), NOW(),
       datos.nombres, @clave_hash, datos.rol, datos.telefono
FROM (
    SELECT 1 AS activo, 'Huaripata Romero' AS apellidos, '12345678' AS dni,
           'gerson@clinicaguadalupe.com' AS email, 'Gerson Jose' AS nombres,
           'ADMINISTRADOR' AS rol, '999888777' AS telefono
    UNION ALL SELECT 1, 'Torres Vega', '74125896', 'recepcion@clinicaguadalupe.com', 'Lucia Fernanda', 'RECEPCIONISTA', '956789123'
    UNION ALL SELECT 1, 'Perez Vargas', '87654321', 'paciente@clinicaguadalupe.com', 'Juan Carlos', 'PACIENTE', '912345678'
    UNION ALL SELECT 1, 'Torres Diaz', '76543210', 'maria@clinicaguadalupe.com', 'Maria Elena', 'PACIENTE', '923456789'
    UNION ALL SELECT 1, 'Romero Graciano', '88994455', 'yudy.romero@clinicaguadalupe.com', 'Yudy Dionicia', 'PACIENTE', '988231035'
    UNION ALL SELECT 1, 'Salazar Mendoza', '71234567', 'andres.salazar@clinicaguadalupe.com', 'Andrés', 'MEDICO', '987654321'
    UNION ALL SELECT 1, 'Paredes Rojas', '71234568', 'beatriz.paredes@clinicaguadalupe.com', 'Beatriz', 'MEDICO', '987654322'
    UNION ALL SELECT 1, 'Quispe Torres', '71234569', 'carlos.quispe@clinicaguadalupe.com', 'Carlos', 'MEDICO', '987654323'
    UNION ALL SELECT 1, 'Vargas León', '71234570', 'diana.vargas@clinicaguadalupe.com', 'Diana', 'MEDICO', '987654324'
    UNION ALL SELECT 1, 'Mendoza Ruiz', '71234571', 'elena.mendoza@clinicaguadalupe.com', 'Elena', 'MEDICO', '987654325'
    UNION ALL SELECT 1, 'Castillo Díaz', '71234572', 'fernando.castillo@clinicaguadalupe.com', 'Fernando', 'MEDICO', '987654326'
    UNION ALL SELECT 1, 'Navarro Soto', '71234573', 'gabriela.navarro@clinicaguadalupe.com', 'Gabriela', 'MEDICO', '987654327'
    UNION ALL SELECT 1, 'Ramos Flores', '71234574', 'hector.ramos@clinicaguadalupe.com', 'Héctor', 'MEDICO', '987654328'
    UNION ALL SELECT 1, 'Cruz Medina', '71234575', 'irene.cruz@clinicaguadalupe.com', 'Irene', 'MEDICO', '987654329'
    UNION ALL SELECT 1, 'Mora Salas', '71234576', 'jorge.mora@clinicaguadalupe.com', 'Jorge', 'MEDICO', '987654330'
    UNION ALL SELECT 1, 'Fuentes Vega', '71234577', 'karla.fuentes@clinicaguadalupe.com', 'Karla', 'MEDICO', '987654331'
    UNION ALL SELECT 1, 'Huamán Pérez', '71234578', 'luis.huaman@clinicaguadalupe.com', 'Luis', 'MEDICO', '987654332'
    UNION ALL SELECT 1, 'Sánchez Vera', '71234579', 'mariana.sanchez@clinicaguadalupe.com', 'Mariana', 'MEDICO', '987654333'
    UNION ALL SELECT 1, 'Ortega Peña', '71234580', 'nicolas.ortega@clinicaguadalupe.com', 'Nicolás', 'MEDICO', '987654334'
    UNION ALL SELECT 1, 'Zamora Gil', '71234581', 'olivia.zamora@clinicaguadalupe.com', 'Olivia', 'MEDICO', '987654335'
) AS datos
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios u WHERE u.email = datos.email OR u.dni = datos.dni
);

-- 3) Perfiles clínicos de los pacientes.
INSERT INTO pacientes
    (alergias, contacto_emergencia, direccion, fecha_actualizacion,
     fecha_nacimiento, sexo, telefono_emergencia, tipo_sangre, usuario_id)
SELECT
    CASE u.email
        WHEN 'paciente@clinicaguadalupe.com' THEN 'Ninguna conocida'
        WHEN 'maria@clinicaguadalupe.com' THEN 'Paracetamol'
        ELSE 'Ninguna conocida'
    END,
    'Contacto de emergencia', 'Lima, Perú', NOW(),
    CASE u.email
        WHEN 'paciente@clinicaguadalupe.com' THEN '1995-05-15'
        WHEN 'maria@clinicaguadalupe.com' THEN '1998-03-08'
        ELSE '1993-03-08'
    END,
    CASE u.email WHEN 'maria@clinicaguadalupe.com' THEN 'FEMENINO' ELSE 'MASCULINO' END,
    '999000000', 'O+', u.id
FROM usuarios u
WHERE u.rol = 'PACIENTE'
  AND NOT EXISTS (SELECT 1 FROM pacientes p WHERE p.usuario_id = u.id);

-- 4) Perfiles profesionales de los 15 médicos.
INSERT INTO medicos
    (activo, biografia, cmp, duracion_consulta_minutos, fecha_actualizacion,
     fecha_creacion, precio_consulta, especialidad_id, usuario_id)
SELECT 1,
       CONCAT('Médico especialista de Clínica Guadalupe en ', e.nombre, '.'),
       CASE u.email
           WHEN 'andres.salazar@clinicaguadalupe.com' THEN 'CMP71001'
           WHEN 'beatriz.paredes@clinicaguadalupe.com' THEN 'CMP71002'
           WHEN 'carlos.quispe@clinicaguadalupe.com' THEN 'CMP71003'
           WHEN 'diana.vargas@clinicaguadalupe.com' THEN 'CMP71004'
           WHEN 'elena.mendoza@clinicaguadalupe.com' THEN 'CMP71005'
           WHEN 'fernando.castillo@clinicaguadalupe.com' THEN 'CMP71006'
           WHEN 'gabriela.navarro@clinicaguadalupe.com' THEN 'CMP71007'
           WHEN 'hector.ramos@clinicaguadalupe.com' THEN 'CMP71008'
           WHEN 'irene.cruz@clinicaguadalupe.com' THEN 'CMP71009'
           WHEN 'jorge.mora@clinicaguadalupe.com' THEN 'CMP71010'
           WHEN 'karla.fuentes@clinicaguadalupe.com' THEN 'CMP71011'
           WHEN 'luis.huaman@clinicaguadalupe.com' THEN 'CMP71012'
           WHEN 'mariana.sanchez@clinicaguadalupe.com' THEN 'CMP71013'
           WHEN 'nicolas.ortega@clinicaguadalupe.com' THEN 'CMP71014'
           WHEN 'olivia.zamora@clinicaguadalupe.com' THEN 'CMP71015'
       END,
       30, NOW(), NOW(), 120.00, e.id, u.id
FROM usuarios u
JOIN especialidades e ON e.nombre = CASE u.email
    WHEN 'andres.salazar@clinicaguadalupe.com' THEN 'Cardiología'
    WHEN 'beatriz.paredes@clinicaguadalupe.com' THEN 'Pediatría'
    WHEN 'carlos.quispe@clinicaguadalupe.com' THEN 'Ginecología'
    WHEN 'diana.vargas@clinicaguadalupe.com' THEN 'Obstetricia'
    WHEN 'elena.mendoza@clinicaguadalupe.com' THEN 'Medicina interna'
    WHEN 'fernando.castillo@clinicaguadalupe.com' THEN 'Medicina general'
    WHEN 'gabriela.navarro@clinicaguadalupe.com' THEN 'Oftalmología'
    WHEN 'hector.ramos@clinicaguadalupe.com' THEN 'Dermatología'
    WHEN 'irene.cruz@clinicaguadalupe.com' THEN 'Traumatología'
    WHEN 'jorge.mora@clinicaguadalupe.com' THEN 'Neurología'
    WHEN 'karla.fuentes@clinicaguadalupe.com' THEN 'Endocrinología'
    WHEN 'luis.huaman@clinicaguadalupe.com' THEN 'Gastroenterología'
    WHEN 'mariana.sanchez@clinicaguadalupe.com' THEN 'Urología'
    WHEN 'nicolas.ortega@clinicaguadalupe.com' THEN 'Otorrinolaringología'
    WHEN 'olivia.zamora@clinicaguadalupe.com' THEN 'Psiquiatría'
END
WHERE u.rol = 'MEDICO'
  AND NOT EXISTS (SELECT 1 FROM medicos m WHERE m.usuario_id = u.id);

-- 5) Un consultorio para cada médico, evitando cruces de citas.
INSERT INTO consultorios
    (activo, codigo, descripcion, fecha_actualizacion, fecha_creacion, nombre, piso)
SELECT 1, CONCAT('MED-', m.id),
       CONCAT('Consultorio asignado a ', u.nombres, ' ', u.apellidos),
       NOW(), NOW(), CONCAT('Consultorio médico ', m.id), 1
FROM medicos m
JOIN usuarios u ON u.id = m.usuario_id
WHERE NOT EXISTS (
    SELECT 1 FROM consultorios c WHERE c.codigo = CONCAT('MED-', m.id)
);

-- 6) Horarios recurrentes: lunes a domingo, 08:00 a 20:00.
INSERT INTO horarios_medicos
    (activo, consultorio_id, dia_semana, fecha_actualizacion, fecha_creacion,
     hora_fin, hora_inicio, medico_id)
SELECT 1, c.id, d.dia, NOW(), NOW(), '20:00:00', '08:00:00', m.id
FROM medicos m
JOIN consultorios c ON c.codigo = CONCAT('MED-', m.id)
CROSS JOIN (
    SELECT 'LUNES' AS dia
    UNION ALL SELECT 'MARTES'
    UNION ALL SELECT 'MIERCOLES'
    UNION ALL SELECT 'JUEVES'
    UNION ALL SELECT 'VIERNES'
    UNION ALL SELECT 'SABADO'
    UNION ALL SELECT 'DOMINGO'
) d
WHERE NOT EXISTS (
    SELECT 1 FROM horarios_medicos h
    WHERE h.medico_id = m.id AND h.dia_semana = d.dia AND h.activo = 1
);

COMMIT;

-- Verificación final.
SELECT rol, COUNT(*) AS total FROM usuarios GROUP BY rol ORDER BY rol;
SELECT COUNT(*) AS especialidades FROM especialidades;
SELECT COUNT(*) AS medicos FROM medicos;
SELECT COUNT(*) AS consultorios FROM consultorios;
SELECT COUNT(*) AS horarios FROM horarios_medicos;
