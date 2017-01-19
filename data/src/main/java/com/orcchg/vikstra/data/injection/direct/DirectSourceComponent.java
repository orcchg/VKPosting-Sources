package com.orcchg.vikstra.data.injection.direct;

import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DirectSourceModule.class})
public interface DirectSourceComponent {

//    VkontakteEndpoint vkontakteEndpoint();
//    ImageLoader imageLoader();
}
