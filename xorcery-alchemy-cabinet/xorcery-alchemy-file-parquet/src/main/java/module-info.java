module xorcery.alchemy.file.parquet {
    exports dev.xorcery.alchemy.file.parquet.source;

    requires xorcery.alchemy.jar;
    requires xorcery.configuration.api;
    requires xorcery.reactivestreams.api;

    requires org.glassfish.hk2.api;
    requires jakarta.inject;
    requires carpet.record;
    requires org.apache.parquet;
}