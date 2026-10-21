-- Equipamentos adicionais para popular o catálogo em ambiente de desenvolvimento local.
-- identification_photo fica em branco (sem foto real cadastrada); o front já trata fotoUrl
-- vazio mostrando o ícone de placeholder em vez de tentar carregar uma imagem quebrada.
INSERT INTO equipment (name, identification_photo, current_status, project_id, laboratory, quantity, code, category)
VALUES
    ('Arduino Uno R3', '', 'DISPONIVEL', NULL, 'Laboratório 2', 5, 'EQP-ARDUINO-01', 'Microcontrolador'),
    ('Protoboard 830 Pontos', '', 'DISPONIVEL', NULL, 'Laboratório 1', 10, 'EQP-PROTOBOARD-01', 'Acessório'),
    ('Motor Shield L298N', '', 'DISPONIVEL', NULL, 'Laboratório 3', 3, 'EQP-MOTORSHIELD-01', 'Módulo'),
    ('Sensor Ultrassônico HC-SR04', '', 'DISPONIVEL', NULL, 'Laboratório 1', 8, 'EQP-HCSR04-01', 'Sensor'),
    ('Osciloscópio Tektronix', '', 'DISPONIVEL', NULL, 'Laboratório 2', 1, 'EQP-OSCTEK-01', 'Instrumento de medição'),
    ('Raspberry Pi 4', '', 'MANUTENCAO', NULL, 'Laboratório 3', 2, 'EQP-RPI4-01', 'Placa de desenvolvimento'),
    ('Fonte de Bancada', '', 'DISPONIVEL', NULL, 'Laboratório 2', 2, 'EQP-FONTE-01', 'Fonte de alimentação'),
    ('Kit Robótica Educacional', '', 'INATIVO', NULL, 'Laboratório 3', 1, 'EQP-ROBOKIT-01', 'Kit');
