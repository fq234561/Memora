import { env } from '../../utils/env';
import { ImageGenerationProvider } from './types';
import { mockProvider } from './mockProvider';
import { openaiProvider } from './openaiProvider';

function createProvider(): ImageGenerationProvider {
  const providerName = env.IMAGE_GENERATION_PROVIDER;

  switch (providerName) {
    case 'mock':
      return mockProvider;
    case 'openai':
      return openaiProvider;
    default:
      if (env.NODE_ENV === 'production') {
        throw new Error(
          `[imageGeneration] Unknown or unconfigured provider "${providerName}" in production. ` +
            `Set IMAGE_GENERATION_PROVIDER=mock or openai and configure the required credentials.`
        );
      }
      console.warn(
        `[imageGeneration] Unknown provider "${providerName}", falling back to mock (development only)`
      );
      return mockProvider;
  }
}

export const imageGeneration = createProvider();
