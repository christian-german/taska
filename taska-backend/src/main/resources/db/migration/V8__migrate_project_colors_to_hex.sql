UPDATE projects
SET color = CASE color
    WHEN 'berry_red'   THEN '#b8256f'
    WHEN 'red'         THEN '#db4035'
    WHEN 'orange'      THEN '#ff9933'
    WHEN 'yellow'      THEN '#fad000'
    WHEN 'olive_green' THEN '#afb83b'
    WHEN 'lime_green'  THEN '#7ecc49'
    WHEN 'green'       THEN '#299438'
    WHEN 'mint_green'  THEN '#6accbc'
    WHEN 'teal'        THEN '#158fad'
    WHEN 'sky_blue'    THEN '#14aaf5'
    WHEN 'light_blue'  THEN '#96c3eb'
    WHEN 'blue'        THEN '#4073ff'
    WHEN 'grape'       THEN '#884dff'
    WHEN 'violet'      THEN '#af38eb'
    WHEN 'lavender'    THEN '#eb96eb'
    WHEN 'magenta'     THEN '#e05194'
    WHEN 'salmon'      THEN '#ff8d85'
    WHEN 'charcoal'    THEN '#808080'
    WHEN 'grey'        THEN '#b8b8b8'
    WHEN 'taupe'       THEN '#ccac93'
    ELSE color
END;

ALTER TABLE projects
    ALTER COLUMN color SET DEFAULT '#808080';
