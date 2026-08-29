-- ACC-10: DB-based cluster lease to prevent multiple replicas from
-- performing the same scheduled work concurrently.
CREATE TABLE IF NOT EXISTS cluster_lease (
    name        VARCHAR(128) PRIMARY KEY,
    holder      VARCHAR(256) NOT NULL,
    acquired_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL
);
