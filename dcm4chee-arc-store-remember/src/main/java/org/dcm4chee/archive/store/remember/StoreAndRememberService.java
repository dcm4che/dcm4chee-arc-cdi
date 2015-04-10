package org.dcm4chee.archive.store.remember;

import java.util.Collection;

import org.dcm4chee.archive.dto.ArchiveInstanceLocator;
import org.dcm4chee.archive.store.scu.CStoreSCUContext;
import org.dcm4chee.archive.stow.client.StowContext;

public interface StoreAndRememberService {

    void store(StowContext context
            , Collection<ArchiveInstanceLocator> insts);

    void store(CStoreSCUContext context
           , Collection<ArchiveInstanceLocator> insts);

}
