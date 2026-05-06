import { env } from '../../utils/env';
import { ImageGenerationProvider } from './types';
import { mockProvider } from './mockProvider';

function createProvider(): ImageGenerationProvider {
  const providerName = env.IMAGE_GENERATION_PROVIDER;

  switch (providerName) {
    case 'mock':
      return mockProvider;
    default:
      console.warn(
        `[imageGeneration] Unknown provider "${providerName}", falling back to mock`
      );
      return mockProvider;
  }
}

export const imageGeneration = createProvider();
