CREATE OR REPLACE FUNCTION invite_autosource_candidate()
    RETURNS trigger AS
$invite_autosource_candidate$
BEGIN
    IF (select jcm.autosourced from job_candidate_mapping jcm where jcm.id=NEW.jcm_id) THEN
        UPDATE jcm_communication_details set chat_invite_flag = 't' where id = NEW.id;
        UPDATE job_candidate_mapping set stage=(select id from stage_step_master where stage='Screening'), chatbot_status='Invited' where id=NEW.jcm_id;
    END IF;

    RETURN NULL;
END;
$invite_autosource_candidate$
LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS invite_autosource_candidate on jcm_communication_details;
CREATE TRIGGER invite_autosource_candidate
AFTER UPDATE
  ON jcm_communication_details
FOR EACH ROW
WHEN (
OLD.chat_invite_timestamp_sms IS DISTINCT FROM NEW.chat_invite_timestamp_sms
OR
OLD.chat_invite_timestamp_email IS DISTINCT FROM NEW.chat_invite_timestamp_email
)
EXECUTE PROCEDURE invite_autosource_candidate();