-- 세 앱(api/batch/realtime)이 공유하는 스키마의 단일 출처.
-- 이 파일이 테스트 DB와 운영 DB를 모두 만든다.

create table stock (
    id           bigint       not null auto_increment,
    code         varchar(255),
    name         varchar(255),
    stock_regime varchar(32),
    stock_trend  varchar(32),
    created_at   datetime(6)  not null,
    updated_at   datetime(6)  not null,
    deleted_at   datetime(6),
    primary key (id)
);

create table stock_daily_candle (
    id          bigint      not null auto_increment,
    trade_date  date,
    open_price  bigint      not null,
    high_price  bigint      not null,
    low_price   bigint      not null,
    close_price bigint      not null,
    volume      bigint      not null,
    stock_id    bigint,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,
    deleted_at  datetime(6),
    primary key (id),
    constraint fk_stock_daily_candle_stock foreign key (stock_id) references stock (id)
);

create table stock_moving_average (
    id                          bigint      not null auto_increment,
    ma                          bigint      not null,
    base_date                   date,
    stock_moving_average_period varchar(32),
    stock_id                    bigint,
    created_at                  datetime(6) not null,
    updated_at                  datetime(6) not null,
    deleted_at                  datetime(6),
    primary key (id),
    constraint fk_stock_moving_average_stock foreign key (stock_id) references stock (id)
);

create table stock_base_line (
    id                 bigint        not null auto_increment,
    price              bigint,
    strength           decimal(38, 2),
    accumulated_volume bigint,
    touch_count        bigint,
    type               varchar(32),
    stock_base_id      bigint,
    created_at         datetime(6)   not null,
    updated_at         datetime(6)   not null,
    deleted_at         datetime(6),
    primary key (id)
);

create table stock_base (
    id                           bigint      not null auto_increment,
    is_vcp                       boolean,
    started_at                   date,
    stage_level                  bigint      not null,
    stock_base_kind              varchar(32),
    stock_id                     bigint,
    highest_resistance_line_id   bigint unique,
    lowest_support_line_id       bigint unique,
    strongest_resistance_line_id bigint unique,
    strongest_support_line_id    bigint unique,
    created_at                   datetime(6) not null,
    updated_at                   datetime(6) not null,
    deleted_at                   datetime(6),
    primary key (id),
    constraint fk_stock_base_stock foreign key (stock_id) references stock (id),
    constraint fk_stock_base_highest_resistance foreign key (highest_resistance_line_id) references stock_base_line (id),
    constraint fk_stock_base_lowest_support foreign key (lowest_support_line_id) references stock_base_line (id),
    constraint fk_stock_base_strongest_resistance foreign key (strongest_resistance_line_id) references stock_base_line (id),
    constraint fk_stock_base_strongest_support foreign key (strongest_support_line_id) references stock_base_line (id)
);

alter table stock_base_line
    add constraint fk_stock_base_line_stock_base foreign key (stock_base_id) references stock_base (id);

create table stock_anchor_point (
    id            bigint      not null auto_increment,
    price         bigint,
    volume        bigint      not null,
    trade_date    date,
    type          varchar(32),
    stock_id      bigint      not null,
    stock_base_id bigint,
    created_at    datetime(6) not null,
    updated_at    datetime(6) not null,
    deleted_at    datetime(6),
    primary key (id),
    constraint fk_stock_anchor_point_stock foreign key (stock_id) references stock (id),
    constraint fk_stock_anchor_point_stock_base foreign key (stock_base_id) references stock_base (id)
);

create table stock_anchor_point_calculation (
    id                    bigint         not null auto_increment,
    current_price         bigint         not null,
    volume                bigint         not null,
    trade_date            date,
    slope_upper_max       decimal(38, 2),
    slope_lower_min       decimal(38, 2),
    stock_anchor_point_id bigint,
    created_at            datetime(6)    not null,
    updated_at            datetime(6)    not null,
    deleted_at            datetime(6),
    primary key (id),
    constraint fk_stock_anchor_point_calculation_point foreign key (stock_anchor_point_id) references stock_anchor_point (id)
);

create table stock_rank_score (
    id         bigint         not null auto_increment,
    base_date  date,
    momentum   decimal(38, 6),
    fip        decimal(38, 6),
    up_days    int,
    down_days  int,
    stock_id   bigint,
    created_at datetime(6)    not null,
    updated_at datetime(6)    not null,
    deleted_at datetime(6),
    primary key (id),
    constraint fk_stock_rank_score_stock foreign key (stock_id) references stock (id)
);

create table stock_eps (
    id                         bigint      not null auto_increment,
    eps                        double      not null,
    quarter                    date,
    year_over_year_change_rate double,
    stock_id                   bigint,
    created_at                 datetime(6) not null,
    updated_at                 datetime(6) not null,
    deleted_at                 datetime(6),
    primary key (id),
    constraint fk_stock_eps_stock foreign key (stock_id) references stock (id)
);

create table kospi (
    id          bigint      not null auto_increment,
    kospi_value bigint,
    record_date date,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,
    deleted_at  datetime(6),
    primary key (id)
);

create table kospi_relative_strength (
    id         bigint      not null auto_increment,
    rs_score   int         not null,
    kospi_id   bigint,
    stock_id   bigint,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    deleted_at datetime(6),
    primary key (id),
    constraint fk_kospi_relative_strength_kospi foreign key (kospi_id) references kospi (id),
    constraint fk_kospi_relative_strength_stock foreign key (stock_id) references stock (id)
);

create table stock_tick (
    id         bigint      not null auto_increment,
    stock_code varchar(255),
    trade_time datetime(6),
    price      bigint,
    volume     bigint,
    acc_volume bigint,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    deleted_at datetime(6),
    primary key (id)
);

create table member (
    id           bigint      not null auto_increment,
    email        varchar(255),
    password     varchar(255),
    nickname     varchar(255),
    name         varchar(255),
    phone_number varchar(255),
    created_at   datetime(6) not null,
    updated_at   datetime(6) not null,
    deleted_at   datetime(6),
    primary key (id)
);

create table refresh_token (
    id         bigint      not null auto_increment,
    token      varchar(512),
    expires_at datetime(6),
    member_id  bigint,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    deleted_at datetime(6),
    primary key (id)
);

create table stock_like (
    id         bigint      not null auto_increment,
    member_id  bigint,
    stock_id   bigint,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    deleted_at datetime(6),
    primary key (id),
    constraint uk_stock_like_member_stock unique (member_id, stock_id),
    constraint fk_stock_like_member foreign key (member_id) references member (id)
);

create table stock_snap_shot (
    id              bigint       not null auto_increment,
    stock_id        bigint,
    captured_regime varchar(32),
    captured_price  bigint       not null,
    judgment        varchar(32),
    recorded_at     datetime(6),
    retrospective   varchar(2000),
    created_at      datetime(6)  not null,
    updated_at      datetime(6)  not null,
    deleted_at      datetime(6),
    primary key (id)
);

create table snapshot_reference (
    id                     bigint      not null auto_increment,
    snapshot_id            bigint,
    referenced_snapshot_id bigint,
    created_at             datetime(6) not null,
    updated_at             datetime(6) not null,
    deleted_at             datetime(6),
    primary key (id),
    constraint fk_snapshot_reference_snapshot foreign key (snapshot_id) references stock_snap_shot (id),
    constraint fk_snapshot_reference_referenced foreign key (referenced_snapshot_id) references stock_snap_shot (id)
);
