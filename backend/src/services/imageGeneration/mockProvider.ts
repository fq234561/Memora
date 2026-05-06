import { ImageGenerationProvider, GenerationInput, GenerationResult } from './types';

export const mockProvider: ImageGenerationProvider = {
  async generateCandidates(input: GenerationInput): Promise<GenerationResult> {
    const { projectId } = input;

    // Generate 4 candidate images with different seeds
    const candidateUrls = [
      `https://picsum.photos/seed/${projectId}_c1/400/600`,
      `https://picsum.photos/seed/${projectId}_c2/400/600`,
      `https://picsum.photos/seed/${projectId}_c3/400/600`,
      `https://picsum.photos/seed/${projectId}_c4/400/600`,
    ];

    return {
      candidateUrls,
      prompt: input.customPrompt || 'default_prompt',
    };
  },
};
