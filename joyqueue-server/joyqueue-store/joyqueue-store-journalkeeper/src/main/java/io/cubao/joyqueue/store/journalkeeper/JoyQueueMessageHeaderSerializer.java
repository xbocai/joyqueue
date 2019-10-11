package io.cubao.joyqueue.store.journalkeeper;

import io.chubao.joyqueue.store.message.MessageParser;
import io.journalkeeper.base.FixedLengthSerializer;
import io.journalkeeper.core.entry.EntryHeader;

/**
 * @author LiYue
 * Date: 2019/10/11
 */
public class JoyQueueMessageHeaderSerializer implements FixedLengthSerializer<EntryHeader> {
    @Override
    public int serializedEntryLength() {
        return MessageParser.getFixedAttributesLength();
    }

    @Override
    public byte[] serialize(EntryHeader entryHeader) {
        return new byte[0];
    }

    @Override
    public EntryHeader parse(byte[] bytes) {

        return null;
    }
}
