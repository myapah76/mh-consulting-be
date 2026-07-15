package com.mhconsultingbe.shared.util;

import com.mhconsultingbe.shared.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextNormalizerTests {
    @Test void normalizesEmailAndSlugStyleValues() {
        assertEquals("admin@example.com", TextNormalizer.lowercase("  ADMIN@EXAMPLE.COM "));
        assertNull(TextNormalizer.trimToNull("   "));
    }

    @Test void rejectsHtmlButAcceptsMarkdownAndPlainText() {
        assertEquals("**Nội dung**", TextNormalizer.plainText(" **Nội dung** "));
        assertThrows(InvalidRequestException.class, () -> TextNormalizer.plainText("<script>alert(1)</script>"));
    }
}
