export interface GenerationInput {
  projectId: string;
  style: string;
  customPrompt?: string;
  adjustmentPrompt?: string;
  basePhotoUrl?: string;
  personPhotoUrl?: string;
  isRegeneration?: boolean;
}

export interface GenerationResult {
  candidateUrls: string[];
  prompt: string;
}

export interface ImageGenerationProvider {
  generateCandidates(input: GenerationInput): Promise<GenerationResult>;
}
