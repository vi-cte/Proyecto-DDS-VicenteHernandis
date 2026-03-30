BEGIN;

ALTER TABLE public.votes
    ADD COLUMN IF NOT EXISTS participant_id bigint;

UPDATE public.votes v
SET participant_id = p.id
FROM public.participants p
WHERE v.participant_id IS NULL
  AND lower(v.participant_name) = lower(p.team_name);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.votes
        WHERE participant_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Hay votos sin participante relacionado. Revisa votes.participant_name antes de continuar.';
    END IF;
END $$;

ALTER TABLE public.votes
    ALTER COLUMN participant_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_votes_participant'
    ) THEN
        ALTER TABLE public.votes
            ADD CONSTRAINT fk_votes_participant
            FOREIGN KEY (participant_id) REFERENCES public.participants(id);
    END IF;
END $$;

ALTER TABLE public.votes
    DROP COLUMN IF EXISTS participant_name;

COMMIT;
