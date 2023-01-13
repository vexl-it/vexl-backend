package com.cleevio.vexl.common.hibernate.type;

import com.cleevio.vexl.common.hibernate.type.descriptor.SetArrayTypeDescriptor;
import com.vladmihalcea.hibernate.type.array.internal.AbstractArrayType;
import com.vladmihalcea.hibernate.type.util.Configuration;

public class SetArrayType extends AbstractArrayType<Object> {

    public SetArrayType() {
        super(new SetArrayTypeDescriptor());
    }

    public SetArrayType(Configuration configuration) {
        super(new SetArrayTypeDescriptor(), configuration);
    }

    public String getName() {
        return "set-array";
    }
}
