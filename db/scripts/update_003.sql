ALTER TABLE post
DROP CONSTRAINT constraint_text_and_link;
ALTER TABLE post
ADD CONSTRAINT constraint_link UNIQUE (link);