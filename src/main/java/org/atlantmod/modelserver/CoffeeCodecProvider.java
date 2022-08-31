package org.atlantmod.modelserver;

import org.eclipse.emfcloud.modelserver.common.ModelServerPathParameters;
import org.eclipse.emfcloud.modelserver.common.codecs.Codec;
import org.eclipse.emfcloud.modelserver.common.codecs.XmiCodec;
import org.eclipse.emfcloud.modelserver.emf.common.codecs.CodecProvider;
import org.eclipse.emfcloud.modelserver.emf.common.codecs.JsonCodec;
import org.eclipse.emfcloud.modelserver.emf.common.codecs.JsonCodecV2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class CoffeeCodecProvider implements CodecProvider {
    private final Map<String, Supplier<Codec>> supportedFormats = new LinkedHashMap();

    public CoffeeCodecProvider() {
        this.supportedFormats.put("ecore", CoffeeCodec::new);
        this.supportedFormats.put(ModelServerPathParameters.FORMAT_JSON, CoffeeTreeJsonCodec::new);
    }

    public Optional<Codec> getCodec(String modelUri, String format) {
        Supplier<Codec> codecSupplier = (Supplier) this.supportedFormats.get(format);
        return codecSupplier == null ? Optional.empty() : Optional.of((Codec) codecSupplier.get());
    }

    public Set<String> getAllFormats() {
        return this.supportedFormats.keySet();
    }

    public int getPriority(String modelUri, String format) {
        return this.getAllFormats().contains(format) ? 1 : -1;
    }
}
