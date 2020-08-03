CREATE TABLE `currency` (
  `id` text PRIMARY KEY,
  `sign` text
);

CREATE TABLE `tickers` (
  `id` text PRIMARY KEY,
  `url` text,
  `currency` text NOT NULL,
  `name` text,
  `logo` text
);

CREATE TABLE `live_data` (
  `date` date,
  `ticker_id` text,
  `price` double NOT NULL,
  `consensus` double,
  `created` datetime NOT NULL DEFAULT (now()),
  `updated` datetime NOT NULL DEFAULT (now()),
  PRIMARY KEY (`date`, `ticker_id`)
);

CREATE TABLE `users` (
  `id` long PRIMARY KEY AUTO_INCREMENT,
  `chat_id` long UNIQUE NOT NULL,
  `name` text NOT NULL,
  `allow_custom_read` boolean NOT NULL,
  `allow_custom_subscription` boolean NOT NULL,
  `allow_common_subscription` boolean NOT NULL
);

CREATE TABLE `user_ticker_subscription` (
  `user_id` long,
  `ticker_id` text,
  `last_notification_date` date,
  `threshold` double NOT NULL,
  PRIMARY KEY (`user_id`, `ticker_id`)
);

ALTER TABLE `tickers` ADD FOREIGN KEY (`currency`) REFERENCES `currency` (`id`);

ALTER TABLE `live_data` ADD FOREIGN KEY (`ticker_id`) REFERENCES `tickers` (`id`);

ALTER TABLE `user_ticker_subscription` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `user_ticker_subscription` ADD FOREIGN KEY (`ticker_id`) REFERENCES `tickers` (`id`);

