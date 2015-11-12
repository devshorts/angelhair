CREATE TABLE pointer (
  queuename text,
  pointer_type text,
  value bigint,

  PRIMARY KEY (queuename, pointer_type)
);

CREATE TABLE monoton (
  queuename text PRIMARY KEY,
  value bigint
);

CREATE TABLE message (
  queuename text,
  bucket_num bigint,
  monoton bigint,
  message text,
  version int,
  acked boolean,
  next_visible_on timestamp,
  created_date timestamp,

  PRIMARY KEY ((queuename, bucket_num), monoton)
);
