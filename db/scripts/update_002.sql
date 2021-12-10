ALTER TABLE post
ADD CONSTRAINT constraint_text_and_link UNIQUE (text, link);