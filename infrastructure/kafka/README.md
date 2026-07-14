# Local Kafka (KRaft)

Apache Kafka in KRaft mode (no ZooKeeper) for Thabat local development, plus Kafka UI.

| Service | Port | Notes |
|---------|------|--------|
| Kafka broker | `9092` | Spring apps on Windows: `localhost:9092` |
| Kafka UI | `8090` | http://localhost:8090 |

## Start

From this directory:

```bash
docker compose up -d
docker compose ps
```

## Stop (keep data)

```bash
docker compose down
```

## Stop and wipe Kafka volume

```bash
docker compose down -v
```

## Notes

- No authentication in local development.
- Data persists in the Docker volume `thabat-kafka-data`.
- In-container clients (Kafka UI) use `thabat-kafka:29092`; host apps use `localhost:9092`.
