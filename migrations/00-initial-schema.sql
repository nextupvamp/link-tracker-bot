create table if not exists link (
                                    id bigserial not null primary key,
                                    url varchar not null
);

create table if not exists chat (
                                    id bigint not null primary key,
                                    state varchar not null,
                                    curr_edited_link bigint references link (id)
);

create table if not exists chat_links (
                                          chat_id bigint not null references chat (id) on delete cascade,
                                          link_id bigint not null references link (id) on delete cascade
);

create table if not exists link_filters (
                                            link bigint not null references link (id) on delete cascade,
                                            filter varchar not null
);

create table if not exists link_tags (
                                         link bigint not null references link (id) on delete cascade,
                                         tag varchar not null
);

create table if not exists subscription (
                                            url varchar not null primary key,
                                            updated boolean not null,
                                            last_update bigint not null,
                                            site varchar not null
);

create table if not exists subscriber(
                                         chat_id bigint not null references chat (id) on delete cascade,
                                         subscription varchar not null references subscription (url) on delete cascade
);

create index if not exists chat_links_idx on chat_links using btree (chat_id);

create index if not exists link_tags_idx on link_tags using btree (link);

create index if not exists link_filters_idx on link_filters using btree (link);

create index if not exists subscriber_idx on subscriber using btree (subscription);
