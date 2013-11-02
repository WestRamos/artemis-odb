package com.artemis.weaver;

import static com.artemis.meta.ClassMetadataUtil.instanceFields;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.artemis.meta.ClassMetadata;
import com.artemis.meta.ClassMetadataUtil;
import com.artemis.meta.FieldDescriptor;

public class ResetMethodVisitor extends MethodVisitor implements Opcodes {

	private final ClassMetadata meta;

	public ResetMethodVisitor(MethodVisitor mv, ClassMetadata meta) {
		super(ASM4, mv);
		this.meta = meta;
	}

	@Override
	public void visitCode() {
		mv.visitCode();
		for (FieldDescriptor field : instanceFields(meta))
			resetField(field);
	}
	
	private void resetField(FieldDescriptor field) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(constInstructionFor(field));
		mv.visitFieldInsn(PUTFIELD, meta.type.getInternalName(), field.getName(), field.getDesc());
	}

	private static int constInstructionFor(FieldDescriptor field) {
		if ("Z".equals(field.desc))
			return ICONST_0;
		else if ("C".equals(field.desc))
			return ICONST_0;
		else if ("S".equals(field.desc))
			return ICONST_0;
		else if ("I".equals(field.desc))
			return ICONST_0;
		else if ("J".equals(field.desc))
			return LCONST_0;
		else if ("F".equals(field.desc))
			return FCONST_0;
		else if ("D".equals(field.desc))
			return DCONST_0;
		else
			return ACONST_NULL;
	}
}
