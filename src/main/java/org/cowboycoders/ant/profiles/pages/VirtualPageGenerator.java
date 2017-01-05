package org.cowboycoders.ant.profiles.pages;

import java.util.List;

/**
 * Hook to allow an AntPage to generate a page that doesn't get generated directly by
 * @see org.cowboycoders.ant.profiles.common.PageDispatcher
 * Created by fluxoid on 02/01/17.
 */
public interface VirtualPageGenerator extends AntPage {

    public List<AntPage> genVirtualPages();
}
