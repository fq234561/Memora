import OpenAI from 'openai';
import sharp from 'sharp';
import { ImageGenerationProvider, GenerationInput, GenerationResult } from './types';
import { env } from '../../utils/env';
import { uploadFile } from '../storage';

async function addWatermark(buffer: Buffer): Promise<Buffer> {
  const svg = `
    <svg width="400" height="60">
      <text x="200" y="45" font-size="36" fill="white" opacity="0.7" text-anchor="end" font-family="Arial, sans-serif">AI-generated</text>
    </svg>
  `;

  return sharp(buffer)
    .composite([
      {
        input: Buffer.from(svg),
        gravity: 'southeast',
      },
    ])
    .png()
    .toBuffer();
}

export const openaiProvider: ImageGenerationProvider = {
  async generateCandidates(input: GenerationInput): Promise<GenerationResult> {
    if (!env.OPENAI_API_KEY) {
      throw new Error('OPENAI_API_KEY is not configured');
    }

    const openai = new OpenAI({ apiKey: env.OPENAI_API_KEY });

    const prompt = input.customPrompt || `A natural, warm family memory photo in ${input.style} style. High quality, photorealistic.`;

    const candidateUrls: string[] = [];

    for (let i = 0; i < 4; i++) {
      const response = await openai.images.generate({
        model: 'gpt-image-1',
        prompt,
        n: 1,
        size: '1024x1536',
        quality: 'high',
      });

      const url = response.data?.[0]?.url;
      if (!url) {
        throw new Error('OpenAI returned empty image URL');
      }

      // Download the generated image
      const fetchRes = await fetch(url);
      if (!fetchRes.ok) {
        throw new Error(`Failed to download OpenAI image: ${fetchRes.status}`);
      }
      const imageBuffer = Buffer.from(await fetchRes.arrayBuffer());

      // Add watermark
      const watermarked = await addWatermark(imageBuffer);

      // Upload to R2
      const key = `generated/${input.projectId}/candidate_${i}_${Date.now()}.png`;
      await uploadFile(key, watermarked, 'image/png');
      candidateUrls.push(key);
    }

    return {
      candidateUrls,
      prompt,
    };
  },
};
