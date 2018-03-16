CREATE OR REPLACE PACKAGE BODY TEST_PACKAGE IS

  FUNCTION G_PACKAGE_NAME RETURN VARCHAR2 IS
  BEGIN
    RETURN 'TEST_PACKAGE';
  END;

  PROCEDURE TEST_PRC_V1_09(id               IN VARCHAR2,
                                  revision         IN VARCHAR2,
                                  creation_date                IN VARCHAR2,
                                  start_date                   OUT VARCHAR2,
                                  due_date                     OUT VARCHAR2,
                                  escalation_duration          OUT VARCHAR2,
                                  stable_version               OUT VARCHAR2,
                                  disclaim                     OUT VARCHAR2) IS
    l_log_step VARCHAR2(4000) := g_package_name || '.TEST_PRC_V1_09';
    l_log_date DATE := SYSDATE;
  BEGIN
    -- Parameterwerte loggen
    log.log_parameter_einfuegen(id, l_log_step, 'ID', id, 'J', l_log_date, 'INWF');
    log.log_parameter_einfuegen(id, l_log_step, 'REVISION', revision, 'J', l_log_date, 'INWF');
    log.log_parameter_einfuegen(id, l_log_step, 'CREATION_DATE', creation_date, 'J', l_log_date, 'INWF');
  
    IF CALLME(revision, 'value') =
       'TRUE' THEN
      stable_version := 'true';
    ELSE
      stable_version := 'false';
    END IF;
    log.log_parameter_einfuegen(id, l_log_step, 'STABLE_VERSION', stable_version, 'J', l_log_date, 'OUT');
  
    IF stable_version = 'false' THEN
      CALLANOTHER('value', l_log_step, l_log_date);
    END IF;
  END TEST_PRC_V1_09;

END TEST_PACKAGE;
/