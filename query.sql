CREATE SCHEMA `meltube`;

CREATE TABLE `meltube`.`users`
(
    `email`        VARCHAR(50)  NOT NULL,
    `password`     VARCHAR(100) NOT NULL,
    `nickname`     VARCHAR(10)  NOT NULL,
    `contact`      VARCHAR(12)  NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT NOW(),
    `updated_at`   DATETIME     NOT NULL DEFAULT NOW(),
    `deleted_at`   DATETIME     NULL     DEFAULT NULL,
    `iS_admin`     BOOLEAN      NOT NULL DEFAULT FALSE,
    `iS_suspended` BOOLEAN      NOT NULL DEFAULT FALSE,
    `iS_verified`  BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT PRIMARY KEY (`email`),
    CONSTRAINT UNIQUE (`nickname`),
    CONSTRAINT UNIQUE (`contact`)
);


CREATE TABLE `meltube`.`email_tokens`
(
    `user_email` VARCHAR(50)  NOT NULL,
    `key`        VARCHAR(128) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT NOW(),
    `expires_at` DATETIME     NOT NULL,
    `is_used`    BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT PRIMARY KEY (`user_email`, `key`),
    CONSTRAINT FOREIGN KEY (`user_email`) REFERENCES `meltube`.`users` (`email`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);