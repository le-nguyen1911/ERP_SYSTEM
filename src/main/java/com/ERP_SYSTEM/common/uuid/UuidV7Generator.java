package com.ERP_SYSTEM.common.uuid;

import com.fasterxml.uuid.Generators;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class UuidV7Generator implements IdentifierGenerator {
    @Override
    public Object generate(
            SharedSessionContractImplementor session, Object object) {
        return Generators.timeBasedEpochGenerator().generate();
    }
}
