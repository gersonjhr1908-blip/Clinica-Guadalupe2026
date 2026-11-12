USE clinica_guadalupe;

-- Catálogo inicial para el mantenimiento de especialidades.
-- Es seguro ejecutarlo varias veces: no duplica nombres existentes.
INSERT INTO especialidades (nombre, descripcion, activo, fecha_creacion, fecha_actualizacion)
SELECT datos.nombre, datos.descripcion, 1, NOW(), NOW()
FROM (
    SELECT 'Cardiología' AS nombre, 'Prevención, diagnóstico y tratamiento de enfermedades cardiovasculares.' AS descripcion
    UNION ALL SELECT 'Pediatría', 'Atención médica integral para niños y adolescentes.'
    UNION ALL SELECT 'Ginecología', 'Salud integral de la mujer y atención ginecológica.'
    UNION ALL SELECT 'Obstetricia', 'Control del embarazo, parto y puerperio.'
    UNION ALL SELECT 'Medicina interna', 'Diagnóstico y tratamiento integral del paciente adulto.'
    UNION ALL SELECT 'Medicina general', 'Atención primaria, prevención y seguimiento clínico.'
    UNION ALL SELECT 'Oftalmología', 'Evaluación, prevención y tratamiento de enfermedades oculares.'
    UNION ALL SELECT 'Dermatología', 'Diagnóstico y tratamiento de enfermedades de la piel.'
    UNION ALL SELECT 'Traumatología', 'Lesiones y enfermedades del sistema musculoesquelético.'
    UNION ALL SELECT 'Neurología', 'Diagnóstico y tratamiento del sistema nervioso.'
    UNION ALL SELECT 'Endocrinología', 'Trastornos hormonales y metabólicos.'
    UNION ALL SELECT 'Gastroenterología', 'Enfermedades del aparato digestivo.'
    UNION ALL SELECT 'Urología', 'Salud del aparato urinario y sistema reproductor masculino.'
    UNION ALL SELECT 'Otorrinolaringología', 'Enfermedades de oído, nariz y garganta.'
    UNION ALL SELECT 'Psiquiatría', 'Evaluación y tratamiento de la salud mental.'
) AS datos
WHERE NOT EXISTS (
    SELECT 1 FROM especialidades e WHERE LOWER(e.nombre) = LOWER(datos.nombre)
);

SELECT id, nombre, activo FROM especialidades ORDER BY nombre;
