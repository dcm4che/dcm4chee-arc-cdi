package org.dcm4chee.archive.qido.client.impl;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;

import org.dcm4chee.archive.qido.client.QidoClient;
import org.dcm4chee.archive.qido.client.QidoClientService;
import org.dcm4chee.archive.qido.client.QidoContext;

@ApplicationScoped
public class QidoClientServiceImpl implements QidoClientService{

    @Override
    public QidoClient createQidoClient(QidoContext context) {
        return new QidoClient(context);
    }

    @Override
    public Collection<String> verifyStorage(QidoClient client
            , Collection<String> sopInstanceUIDs)
    {
        return client.verifyStorage(sopInstanceUIDs);
    }


}
